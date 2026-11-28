package de.digidrivelog.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import de.digidrivelog.dto.calculation.ConsumptionDto;
import de.digidrivelog.dto.car.CarDto;
import de.digidrivelog.dto.car.CreateCarRequest;
import de.digidrivelog.dto.cost.CreateCostRequest;
import de.digidrivelog.dto.user.CreateUserRequest;
import de.digidrivelog.dto.user.UserDto;
import de.digidrivelog.models.CostCategory;
import de.digidrivelog.models.CostType;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostCategoryRepository;
import de.digidrivelog.repositories.CostRepository;
import de.digidrivelog.repositories.DriveRepository;
import de.digidrivelog.repositories.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
class ConsumptionServiceIntegrationTest {

    @Autowired private ConsumptionService consumptionService;
    @Autowired private CostService costService;
    @Autowired private CarService carService;
    @Autowired private UserService userService;
    @Autowired private CostRepository costRepository;
    @Autowired private CostCategoryRepository costCategoryRepository;
    @Autowired private DriveRepository driveRepository;
    @Autowired private CarRepository carRepository;
    @Autowired private UserRepository userRepository;

    private CarDto car;
    private UserDto user;
    private CostCategory fuel;
    private CostCategory insurance;

    @BeforeEach
    void setUp() {
        costRepository.deleteAll();
        costCategoryRepository.deleteAll();
        driveRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();

        user = userService.createUser(new CreateUserRequest("Con", "Sumer", true, LocalDate.of(1990, 1, 1)));
        car = carService.createCar(new CreateCarRequest("Car", "M-CS-1", user.getUserId(), null));
        fuel = costCategoryRepository.save(
                new CostCategory(null, "Fuel", CostType.VARIABLE, "Liter", true, true, null, null));
        insurance = costCategoryRepository.save(
                new CostCategory(null, "Insurance", CostType.FIXED, "pcs", false, false, null, null));
    }

    @Test
    void intervals_useLitersOfLaterFillUpOverDistance_firstIsBaseline() {
        fill(fuel, "40.00", 10000, LocalDate.of(2024, 12, 20));
        fill(fuel, "30.00", 10500, LocalDate.of(2025, 1, 10));   // 30 / 500 * 100 = 6.00
        fill(fuel, "50.00", 11000, LocalDate.of(2025, 2, 10));   // 50 / 500 * 100 = 10.00
        fill(insurance, "100.00", 11500, LocalDate.of(2025, 3, 1)); // ignored: not fuel

        ConsumptionDto all = consumptionService.getConsumption(car.getCarId(), null);

        assertThat(all.getIntervals()).hasSize(2);
        assertThat(all.getIntervals().get(0).getLitersPer100Km()).isEqualByComparingTo("6.00");
        assertThat(all.getIntervals().get(1).getLitersPer100Km()).isEqualByComparingTo("10.00");
        assertThat(all.getTotalDistanceKm()).isEqualTo(1000);
        assertThat(all.getAverageLitersPer100Km()).isEqualByComparingTo("8.00");
    }

    @Test
    void yearFilter_appliesToLaterFillUp_baselineMayBeFromPreviousYear() {
        fill(fuel, "40.00", 10000, LocalDate.of(2024, 12, 20));
        fill(fuel, "30.00", 10500, LocalDate.of(2025, 1, 10));

        assertThat(consumptionService.getConsumption(car.getCarId(), 2025).getIntervals()).hasSize(1);
        assertThat(consumptionService.getConsumption(car.getCarId(), 2024).getIntervals()).isEmpty();
    }

    @Test
    void fewerThanTwoFillUps_yieldsNoAverage() {
        fill(fuel, "40.00", 10000, LocalDate.of(2025, 1, 1));

        ConsumptionDto result = consumptionService.getConsumption(car.getCarId(), null);

        assertThat(result.getIntervals()).isEmpty();
        assertThat(result.getAverageLitersPer100Km()).isNull();
    }

    @Test
    void unknownCar_throwsNotFound() {
        assertThatThrownBy(() -> consumptionService.getConsumption(999_999L, null))
                .isInstanceOf(ResponseStatusException.class);
    }

    private void fill(CostCategory category, String liters, int odometer, LocalDate date) {
        costService.createCost(new CreateCostRequest(car.getCarId(), user.getUserId(),
                category.getCostCategoryId(), new BigDecimal("50.00"), new BigDecimal(liters), date, odometer, null));
    }
}
