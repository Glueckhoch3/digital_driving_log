package de.digidrivelog.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.digidrivelog.dto.calculation.CombinedSettlementRowDto;
import de.digidrivelog.dto.calculation.FactorRowDto;
import de.digidrivelog.dto.calculation.ParticipantUpdateRequest;
import de.digidrivelog.dto.calculation.YearlySettlementRowDto;
import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.cost.CreateCostRequest;
import de.digidrivelog.dto.drive.CreateDriveRequest;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.models.CostType;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostDistributionLogYearRepository;
import de.digidrivelog.repositories.CostRepository;
import de.digidrivelog.repositories.CostTotalCarYearRepository;
import de.digidrivelog.repositories.DriveAccountYearRepository;
import de.digidrivelog.repositories.DriveLogMonthTotalRepository;
import de.digidrivelog.repositories.DriveRepository;
import de.digidrivelog.repositories.ExpensesUserYearRepository;
import de.digidrivelog.repositories.UserCostFactorYearRepository;
import de.digidrivelog.repositories.UserRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

/**
 * Exercises the full pipeline (issue #20): monthly aggregation, yearly factors, and the
 * combined settlement — with the emphasis on the money netting to exactly zero.
 */
@SpringBootTest
@ActiveProfiles("test")
class CalculationServiceIntegrationTest {

    @Autowired private CalculationService calculationService;
    @Autowired private CarService carService;
    @Autowired private UserService userService;
    @Autowired private DriveService driveService;
    @Autowired private CostService costService;
    @Autowired private CarRepository carRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DriveRepository driveRepository;
    @Autowired private CostRepository costRepository;
    @Autowired private DriveLogMonthTotalRepository monthTotalRepository;
    @Autowired private DriveAccountYearRepository accountYearRepository;
    @Autowired private CostTotalCarYearRepository costTotalRepository;
    @Autowired private UserCostFactorYearRepository factorRepository;
    @Autowired private ExpensesUserYearRepository expensesRepository;
    @Autowired private CostDistributionLogYearRepository logRepository;

    private static final int YEAR = 2025;

    private CarDto car;
    private UserDto anna;
    private UserDto ben;
    private UserDto carla;

    @BeforeEach
    void setUp() {
        logRepository.deleteAll();
        expensesRepository.deleteAll();
        factorRepository.deleteAll();
        accountYearRepository.deleteAll();
        costTotalRepository.deleteAll();
        monthTotalRepository.deleteAll();
        costRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

        anna = createUser("Anna", "Becker");
        ben = createUser("Ben", "Schulz");
        carla = createUser("Carla", "Weber");
        car = carService.createCar(new CreateCarRequest("Golf", "WOB-AB-123", anna.getUserId(), null));

        // Odometer sequence: Anna drives 100 km in January, Ben drives 200 km in February.
        createDrive(anna, 1000, LocalDate.of(YEAR, 1, 5));
        createDrive(anna, 1100, LocalDate.of(YEAR, 1, 20));
        createDrive(ben, 1300, LocalDate.of(YEAR, 2, 10));

        // Anna paid the variable (fuel) cost, Ben paid the fixed (insurance) cost.
        createCost(anna, "Fuel", "90.00", CostType.VARIABLE, LocalDate.of(YEAR, 1, 20));
        createCost(ben, "Insurance", "300.00", CostType.FIXED, LocalDate.of(YEAR, 3, 1));
    }

    @Test
    void monthlyAggregation_bucketsDistancePerDriver() {
        assertThat(calculationService.monthlyExists(car.getCarId(), YEAR, 1)).isFalse();
        calculationService.aggregateMonth(car.getCarId(), YEAR, 1);
        calculationService.aggregateMonth(car.getCarId(), YEAR, 2);
        assertThat(calculationService.monthlyExists(car.getCarId(), YEAR, 1)).isTrue();

        var distances = calculationService.getMonthlyDistances(car.getCarId(), YEAR);
        // January: only Anna's 100 km (her first drive has no predecessor and is skipped).
        assertThat(distances).anySatisfy(d -> {
            assertThat(d.getMonth()).isEqualTo(1);
            assertThat(d.getUserId()).isEqualTo(anna.getUserId());
            assertThat(d.getDistance()).isEqualTo(100);
        });
        assertThat(distances).anySatisfy(d -> {
            assertThat(d.getMonth()).isEqualTo(2);
            assertThat(d.getUserId()).isEqualTo(ben.getUserId());
            assertThat(d.getDistance()).isEqualTo(200);
        });
    }

    @Test
    void aggregatingSameMonthTwice_conflicts() {
        calculationService.aggregateMonth(car.getCarId(), YEAR, 1);
        assertThatThrownBy(() -> calculationService.aggregateMonth(car.getCarId(), YEAR, 1))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void yearlyCalculation_derivesFactorsAndOwedSplit() {
        aggregateAndCalculate();

        Map<Long, FactorRowDto> factors = calculationService.getFactors(car.getCarId(), YEAR).stream()
                .collect(Collectors.toMap(FactorRowDto::getUserId, Function.identity()));
        // Variable factor follows distance (100 / 300, remainder on the last driver).
        assertThat(factors.get(anna.getUserId()).getFactorVariableCost()).isEqualByComparingTo("33.33");
        assertThat(factors.get(ben.getUserId()).getFactorVariableCost()).isEqualByComparingTo("66.67");
        // Fixed factor is an equal split.
        assertThat(factors.get(anna.getUserId()).getFactorFixCost()).isEqualByComparingTo("50.00");
        assertThat(factors.get(ben.getUserId()).getFactorFixCost()).isEqualByComparingTo("50.00");

        Map<Long, YearlySettlementRowDto> owed = calculationService
                .getYearlySettlement(car.getCarId(), YEAR).stream()
                .collect(Collectors.toMap(YearlySettlementRowDto::getUserId, Function.identity()));
        assertThat(owed.get(anna.getUserId()).getVariableOwed()).isEqualByComparingTo("30.00");
        assertThat(owed.get(ben.getUserId()).getVariableOwed()).isEqualByComparingTo("60.00");
        assertThat(owed.get(anna.getUserId()).getFixedOwed()).isEqualByComparingTo("150.00");
        assertThat(owed.get(ben.getUserId()).getFixedOwed()).isEqualByComparingTo("150.00");
    }

    @Test
    void combinedSettlement_netsToZero() {
        aggregateAndCalculate();

        List<CombinedSettlementRowDto> combined = calculationService.getCombined(YEAR);
        Map<Long, CombinedSettlementRowDto> byUser = combined.stream()
                .collect(Collectors.toMap(CombinedSettlementRowDto::getUserId, Function.identity()));

        // Anna: paid 90 var, owes 30 var + 150 fix -> +60 var, -150 fix, net -90.
        assertThat(byUser.get(anna.getUserId()).getDifferenceVariableCost()).isEqualByComparingTo("60.00");
        assertThat(byUser.get(anna.getUserId()).getDifferenceFixCost()).isEqualByComparingTo("-150.00");
        assertThat(byUser.get(anna.getUserId()).getNetBalance()).isEqualByComparingTo("-90.00");
        // Ben: paid 300 fix, owes 60 var + 150 fix -> -60 var, +150 fix, net +90.
        assertThat(byUser.get(ben.getUserId()).getNetBalance()).isEqualByComparingTo("90.00");

        assertThat(sum(combined, CombinedSettlementRowDto::getDifferenceVariableCost)).isZero();
        assertThat(sum(combined, CombinedSettlementRowDto::getDifferenceFixCost)).isZero();
        assertThat(sum(combined, CombinedSettlementRowDto::getNetBalance)).isZero();
    }

    @Test
    void yearlyCalculation_aggregatesMissingMonthsFirst() {
        // Only January was aggregated manually; February must be filled in by the yearly run.
        calculationService.aggregateMonth(car.getCarId(), YEAR, 1);
        calculationService.calculateYear(car.getCarId(), YEAR);

        assertThat(calculationService.monthlyExists(car.getCarId(), YEAR, 2)).isTrue();
        Map<Long, YearlySettlementRowDto> owed = calculationService
                .getYearlySettlement(car.getCarId(), YEAR).stream()
                .collect(Collectors.toMap(YearlySettlementRowDto::getUserId, Function.identity()));
        assertThat(owed.get(anna.getUserId()).getDistance()).isEqualTo(100);
        assertThat(owed.get(ben.getUserId()).getDistance()).isEqualTo(200);
    }

    @Test
    void calculatingYearTwice_conflicts() {
        aggregateAndCalculate();
        assertThatThrownBy(() -> calculationService.calculateYear(car.getCarId(), YEAR))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(e -> assertThat(((ResponseStatusException) e).getStatusCode())
                        .isEqualTo(HttpStatus.CONFLICT));
    }

    @Test
    void deletingYear_clearsCombinedLog() {
        aggregateAndCalculate();
        assertThat(calculationService.getCombined(YEAR)).isNotEmpty();

        calculationService.deleteYear(car.getCarId(), YEAR);

        assertThat(calculationService.yearlyExists(car.getCarId(), YEAR)).isFalse();
        assertThat(calculationService.getCombined(YEAR)).isEmpty();
    }

    @Test
    void addedNonDriver_getsEqualFixedShareAndZeroVariable() {
        aggregateMonths();
        calculationService.saveParticipants(new ParticipantUpdateRequest(
                car.getCarId(), YEAR, List.of(anna.getUserId(), ben.getUserId(), carla.getUserId())));
        calculationService.calculateYear(car.getCarId(), YEAR);

        Map<Long, FactorRowDto> factors = calculationService.getFactors(car.getCarId(), YEAR).stream()
                .collect(Collectors.toMap(FactorRowDto::getUserId, Function.identity()));
        assertThat(factors.get(anna.getUserId()).getFactorFixCost()).isEqualByComparingTo("33.33");
        assertThat(factors.get(ben.getUserId()).getFactorFixCost()).isEqualByComparingTo("33.33");
        assertThat(factors.get(carla.getUserId()).getFactorFixCost()).isEqualByComparingTo("33.34");
        assertThat(factors.get(carla.getUserId()).getFactorVariableCost()).isEqualByComparingTo("0.00");

        Map<Long, YearlySettlementRowDto> owed = calculationService
                .getYearlySettlement(car.getCarId(), YEAR).stream()
                .collect(Collectors.toMap(YearlySettlementRowDto::getUserId, Function.identity()));
        assertThat(owed.get(carla.getUserId()).getVariableOwed()).isEqualByComparingTo("0.00");
        assertThat(owed.get(carla.getUserId()).getDistance()).isZero();
        assertThat(owed.get(carla.getUserId()).getFixedOwed()).isGreaterThan(java.math.BigDecimal.ZERO);
    }

    @Test
    void noStoredParticipantSet_behavesLikeBeforeTheFeature() {
        aggregateAndCalculate();
        Map<Long, FactorRowDto> factors = calculationService.getFactors(car.getCarId(), YEAR).stream()
                .collect(Collectors.toMap(FactorRowDto::getUserId, Function.identity()));
        assertThat(factors).containsOnlyKeys(anna.getUserId(), ben.getUserId());
        assertThat(factors.get(anna.getUserId()).getFactorFixCost()).isEqualByComparingTo("50.00");
    }

    @Test
    void addedParticipant_appearsInDriveAccountYearWithZeroDistance() {
        aggregateMonths();
        calculationService.saveParticipants(new ParticipantUpdateRequest(
                car.getCarId(), YEAR, List.of(anna.getUserId(), ben.getUserId(), carla.getUserId())));
        calculationService.calculateYear(car.getCarId(), YEAR);

        assertThat(accountYearRepository.findByYearAndCarId(YEAR, car.getCarId()))
                .anySatisfy(a -> {
                    assertThat(a.getUserId()).isEqualTo(carla.getUserId());
                    assertThat(a.getTotalDistanceYear()).isZero();
                });
    }

    @Test
    void combinedSettlement_netsToZeroWithZeroDistanceParticipant() {
        aggregateMonths();
        calculationService.saveParticipants(new ParticipantUpdateRequest(
                car.getCarId(), YEAR, List.of(anna.getUserId(), ben.getUserId(), carla.getUserId())));
        calculationService.calculateYear(car.getCarId(), YEAR);

        List<CombinedSettlementRowDto> combined = calculationService.getCombined(YEAR);
        assertThat(sum(combined, CombinedSettlementRowDto::getDifferenceVariableCost)).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.001));
        assertThat(sum(combined, CombinedSettlementRowDto::getDifferenceFixCost)).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.001));
        assertThat(sum(combined, CombinedSettlementRowDto::getNetBalance)).isCloseTo(0.0, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    void deleteYear_keepsManualRowsResetToZero_andRemovesRunProducedRows() {
        aggregateMonths();
        calculationService.saveParticipants(new ParticipantUpdateRequest(
                car.getCarId(), YEAR, List.of(anna.getUserId(), ben.getUserId(), carla.getUserId())));
        calculationService.calculateYear(car.getCarId(), YEAR);

        calculationService.deleteYear(car.getCarId(), YEAR);

        List<de.digidrivelog.models.UserCostFactorYear> remaining =
                factorRepository.findByYearAndCarId(YEAR, car.getCarId());
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getUserId()).isEqualTo(carla.getUserId());
        assertThat(remaining.get(0).getManuallyAdded()).isTrue();
        assertThat(remaining.get(0).getFactorFixCost()).isEqualByComparingTo("0.00");
        assertThat(remaining.get(0).getFactorVariableCost()).isEqualByComparingTo("0.00");
    }

    @Test
    void deleteThenCalculateYear_withAddedParticipant_survivesTheRoundTrip() {
        aggregateMonths();
        calculationService.saveParticipants(new ParticipantUpdateRequest(
                car.getCarId(), YEAR, List.of(anna.getUserId(), ben.getUserId(), carla.getUserId())));
        calculationService.calculateYear(car.getCarId(), YEAR);
        Map<Long, FactorRowDto> before = calculationService.getFactors(car.getCarId(), YEAR).stream()
                .collect(Collectors.toMap(FactorRowDto::getUserId, Function.identity()));

        calculationService.deleteYear(car.getCarId(), YEAR);
        calculationService.calculateYear(car.getCarId(), YEAR);

        Map<Long, FactorRowDto> after = calculationService.getFactors(car.getCarId(), YEAR).stream()
                .collect(Collectors.toMap(FactorRowDto::getUserId, Function.identity()));
        assertThat(after.keySet()).isEqualTo(before.keySet());
        assertThat(after.get(carla.getUserId()).getFactorFixCost())
                .isEqualByComparingTo(before.get(carla.getUserId()).getFactorFixCost());
    }

    @Test
    void manuallyAddedFlag_survivesTheRun() {
        aggregateMonths();
        calculationService.saveParticipants(new ParticipantUpdateRequest(
                car.getCarId(), YEAR, List.of(anna.getUserId(), ben.getUserId(), carla.getUserId())));
        calculationService.calculateYear(car.getCarId(), YEAR);

        de.digidrivelog.models.UserCostFactorYear carlaRow = factorRepository
                .findByYearAndCarId(YEAR, car.getCarId()).stream()
                .filter(f -> f.getUserId().equals(carla.getUserId()))
                .findFirst().orElseThrow();
        assertThat(carlaRow.getManuallyAdded()).isTrue();
    }

    // --- helpers ---

    private void aggregateAndCalculate() {
        aggregateMonths();
        calculationService.calculateYear(car.getCarId(), YEAR);
    }

    private void aggregateMonths() {
        calculationService.aggregateMonth(car.getCarId(), YEAR, 1);
        calculationService.aggregateMonth(car.getCarId(), YEAR, 2);
    }

    private double sum(List<CombinedSettlementRowDto> rows,
            Function<CombinedSettlementRowDto, java.math.BigDecimal> field) {
        return rows.stream().map(field).mapToDouble(java.math.BigDecimal::doubleValue).sum();
    }

    private UserDto createUser(String first, String last) {
        return userService.createUser(new CreateUserRequest(first, last, true, LocalDate.of(1990, 1, 1)));
    }

    private void createDrive(UserDto driver, int odometer, LocalDate date) {
        driveService.createDrive(new CreateDriveRequest(car.getCarId(), odometer, driver.getUserId(), date, null));
    }

    private void createCost(UserDto buyer, String description, String price, CostType type, LocalDate date) {
        costService.createCost(new CreateCostRequest(car.getCarId(), buyer.getUserId(), description,
                new java.math.BigDecimal(price), java.math.BigDecimal.ONE, date, type, null));
    }
}
