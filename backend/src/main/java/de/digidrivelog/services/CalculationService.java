package de.digidrivelog.services;

import de.digidrivelog.dto.calculation.CombinedSettlementRowDto;
import de.digidrivelog.dto.calculation.FactorRowDto;
import de.digidrivelog.dto.calculation.MonthlyDistanceDto;
import de.digidrivelog.dto.calculation.YearlySettlementRowDto;
import de.digidrivelog.models.CostDistributionLogYear;
import de.digidrivelog.models.CostTotalCarYear;
import de.digidrivelog.models.CostType;
import de.digidrivelog.models.Drive;
import de.digidrivelog.models.DriveAccountYear;
import de.digidrivelog.models.DriveLogMonthTotal;
import de.digidrivelog.models.ExpensesUserYear;
import de.digidrivelog.models.User;
import de.digidrivelog.models.UserCostFactorYear;
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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Owns the yearly/monthly calculation pipeline (issue #20). See ADR-015 for the
 * distribution rules: variable costs split by distance share, fixed costs by an equal
 * default factor, all money in {@link BigDecimal} with the rounding remainder pushed
 * onto the last driver so every column nets to exactly zero.
 */
@Service
@RequiredArgsConstructor
public class CalculationService {

    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO_MONEY = new BigDecimal("0.00");

    private final DriveRepository driveRepository;
    private final CostRepository costRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final DriveLogMonthTotalRepository monthTotalRepository;
    private final DriveAccountYearRepository accountYearRepository;
    private final CostTotalCarYearRepository costTotalRepository;
    private final UserCostFactorYearRepository factorRepository;
    private final ExpensesUserYearRepository expensesRepository;
    private final CostDistributionLogYearRepository logRepository;

    // ---------------------------------------------------------------- actions

    /** Lightweight step: aggregate a car's drives into per-driver monthly distances. */
    @Transactional
    public void aggregateMonth(Long carId, Integer year, Integer month) {
        requireCar(carId);
        if (monthTotalRepository.existsByYearAndMonthAndCarId(year, month, carId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Month already aggregated. Delete it before recalculating.");
        }
        Map<Long, Integer> distanceByDriver = monthlyDistanceByDriver(carId, year, month);
        List<DriveLogMonthTotal> rows = new ArrayList<>();
        distanceByDriver.forEach((userId, distance) ->
                rows.add(new DriveLogMonthTotal(year, month, userId, carId, distance)));
        monthTotalRepository.saveAll(rows);
    }

    /** Full yearly run: rolls months up, totals costs, derives factors, logs the settlement. */
    @Transactional
    public void calculateYear(Long carId, Integer year) {
        requireCar(carId);
        if (costTotalRepository.existsByYearAndCarId(year, carId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Year already calculated for this car. Delete it before recalculating.");
        }

        // 1. Aggregate every month that has drives but was never run, then roll the
        //    aggregated months up into per-driver yearly distances.
        aggregateMissingMonths(carId, year);
        Map<Long, Integer> distanceByDriver = new LinkedHashMap<>();
        for (DriveLogMonthTotal m : monthTotalRepository.findByYearAndCarIdOrderByMonthAsc(year, carId)) {
            distanceByDriver.merge(m.getUserId(), m.getTotalDistanceMonth(), Integer::sum);
        }

        // 2. Cost pools for the car.
        BigDecimal varTotal = costRepository.sumPriceByCarYearAndType(carId, year, CostType.VARIABLE);
        BigDecimal fixTotal = costRepository.sumPriceByCarYearAndType(carId, year, CostType.FIXED);
        costTotalRepository.save(new CostTotalCarYear(year, carId, money(fixTotal), money(varTotal)));

        // 3. The car's driver group for the year, in a stable order (remainder lands on the last).
        List<Long> drivers = new ArrayList<>(new TreeSet<>(distanceByDriver.keySet()));

        // 4. Persist yearly distances.
        List<DriveAccountYear> accountRows = new ArrayList<>();
        for (Long userId : drivers) {
            accountRows.add(new DriveAccountYear(year, userId, carId,
                    distanceByDriver.getOrDefault(userId, 0)));
        }
        accountYearRepository.saveAll(accountRows);

        // 5. Factors as percentages summing to exactly 100.00.
        Map<Long, BigDecimal> distanceWeights = new LinkedHashMap<>();
        drivers.forEach(u -> distanceWeights.put(u, BigDecimal.valueOf(distanceByDriver.getOrDefault(u, 0))));
        Map<Long, BigDecimal> variableFactors = percentagesByWeight(drivers, distanceWeights);
        Map<Long, BigDecimal> fixedFactors = equalPercentages(drivers);

        List<UserCostFactorYear> factorRows = new ArrayList<>();
        for (Long userId : drivers) {
            factorRows.add(new UserCostFactorYear(year, userId, carId,
                    variableFactors.get(userId), fixedFactors.get(userId)));
        }
        factorRepository.saveAll(factorRows);

        // 6. Rebuild the combined settlement across all calculated cars for the year.
        recomputeCombined(year);
    }

    @Transactional
    public void deleteMonth(Long carId, Integer year, Integer month) {
        monthTotalRepository.deleteByYearAndMonthAndCarId(year, month, carId);
    }

    @Transactional
    public void deleteYear(Long carId, Integer year) {
        accountYearRepository.deleteByYearAndCarId(year, carId);
        factorRepository.deleteByYearAndCarId(year, carId);
        costTotalRepository.deleteByYearAndCarId(year, carId);
        recomputeCombined(year);
    }

    // ----------------------------------------------------------------- checks

    @Transactional(readOnly = true)
    public boolean monthlyExists(Long carId, Integer year, Integer month) {
        return monthTotalRepository.existsByYearAndMonthAndCarId(year, month, carId);
    }

    @Transactional(readOnly = true)
    public boolean yearlyExists(Long carId, Integer year) {
        return costTotalRepository.existsByYearAndCarId(year, carId);
    }

    // ------------------------------------------------------------------ views

    @Transactional(readOnly = true)
    public List<MonthlyDistanceDto> getMonthlyDistances(Long carId, Integer year) {
        Map<Long, String> names = userNames();
        List<MonthlyDistanceDto> out = new ArrayList<>();
        for (DriveLogMonthTotal m : monthTotalRepository.findByYearAndCarIdOrderByMonthAsc(year, carId)) {
            out.add(new MonthlyDistanceDto(m.getMonth(), m.getUserId(),
                    names.get(m.getUserId()), m.getTotalDistanceMonth()));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<YearlySettlementRowDto> getYearlySettlement(Long carId, Integer year) {
        CostTotalCarYear totals = costTotalRepository
                .findById(new CostTotalCarYear.Id(year, carId))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "No yearly calculation for this car and year."));

        List<UserCostFactorYear> factors = factorRepository.findByYearAndCarId(year, carId);
        List<Long> drivers = factors.stream().map(UserCostFactorYear::getUserId).sorted().toList();

        Map<Long, Integer> distanceByUser = new LinkedHashMap<>();
        for (DriveAccountYear a : accountYearRepository.findByYearAndCarId(year, carId)) {
            distanceByUser.put(a.getUserId(), a.getTotalDistanceYear());
        }
        Map<Long, BigDecimal> varFactorByUser = new LinkedHashMap<>();
        Map<Long, BigDecimal> fixFactorByUser = new LinkedHashMap<>();
        for (UserCostFactorYear f : factors) {
            varFactorByUser.put(f.getUserId(), f.getFactorVariableCost());
            fixFactorByUser.put(f.getUserId(), f.getFactorFixCost());
        }

        Map<Long, BigDecimal> variableOwed = allocate(totals.getVarTotal(), drivers, varFactorByUser);
        Map<Long, BigDecimal> fixedOwed = allocate(totals.getFixTotal(), drivers, fixFactorByUser);

        Map<Long, String> names = userNames();
        List<YearlySettlementRowDto> out = new ArrayList<>();
        for (Long userId : drivers) {
            BigDecimal varOwed = variableOwed.getOrDefault(userId, ZERO_MONEY);
            BigDecimal fixOwed = fixedOwed.getOrDefault(userId, ZERO_MONEY);
            out.add(new YearlySettlementRowDto(userId, names.get(userId),
                    distanceByUser.getOrDefault(userId, 0),
                    varFactorByUser.get(userId), fixFactorByUser.get(userId),
                    varOwed, fixOwed, varOwed.add(fixOwed)));
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<FactorRowDto> getFactors(Long carId, Integer year) {
        Map<Long, String> names = userNames();
        return factorRepository.findByYearAndCarId(year, carId).stream()
                .sorted((a, b) -> Long.compare(a.getUserId(), b.getUserId()))
                .map(f -> new FactorRowDto(f.getUserId(), names.get(f.getUserId()),
                        f.getFactorVariableCost(), f.getFactorFixCost()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CombinedSettlementRowDto> getCombined(Integer year) {
        Map<Long, BigDecimal> paidByUser = new LinkedHashMap<>();
        for (ExpensesUserYear e : expensesRepository.findByYear(year)) {
            paidByUser.put(e.getUserId(), e.getSpentTotal());
        }
        Map<Long, String> names = userNames();
        List<CombinedSettlementRowDto> out = new ArrayList<>();
        for (CostDistributionLogYear log : logRepository.findByYear(year)) {
            BigDecimal net = log.getDifferenceVariableCost().add(log.getDifferenceFixCost());
            out.add(new CombinedSettlementRowDto(log.getUserId(), names.get(log.getUserId()),
                    paidByUser.getOrDefault(log.getUserId(), ZERO_MONEY),
                    log.getDifferenceVariableCost(), log.getDifferenceFixCost(), net));
        }
        out.sort((a, b) -> Long.compare(a.getUserId(), b.getUserId()));
        return out;
    }

    // --------------------------------------------------------------- internals

    /**
     * Rebuilds the year-global {@code expenses_user_year} and {@code cost_distribution_log_year}
     * from every car that currently has a completed yearly calculation. Owed and paid are both
     * restricted to that set of cars, so each difference column nets to exactly zero.
     */
    private void recomputeCombined(Integer year) {
        expensesRepository.deleteByYear(year);
        logRepository.deleteByYear(year);

        List<CostTotalCarYear> calculatedCars = costTotalRepository.findByYear(year);
        if (calculatedCars.isEmpty()) {
            return;
        }
        List<Long> carIds = calculatedCars.stream().map(CostTotalCarYear::getCarId).toList();

        // owed per user, allocated exactly per car so the totals reconcile.
        Map<Long, BigDecimal> owedVar = new LinkedHashMap<>();
        Map<Long, BigDecimal> owedFix = new LinkedHashMap<>();
        for (CostTotalCarYear car : calculatedCars) {
            List<UserCostFactorYear> factors = factorRepository.findByYearAndCarId(year, car.getCarId());
            List<Long> drivers = factors.stream().map(UserCostFactorYear::getUserId).sorted().toList();
            Map<Long, BigDecimal> varFactor = new LinkedHashMap<>();
            Map<Long, BigDecimal> fixFactor = new LinkedHashMap<>();
            for (UserCostFactorYear f : factors) {
                varFactor.put(f.getUserId(), f.getFactorVariableCost());
                fixFactor.put(f.getUserId(), f.getFactorFixCost());
            }
            allocate(car.getVarTotal(), drivers, varFactor)
                    .forEach((u, amt) -> owedVar.merge(u, amt, BigDecimal::add));
            allocate(car.getFixTotal(), drivers, fixFactor)
                    .forEach((u, amt) -> owedFix.merge(u, amt, BigDecimal::add));
        }

        // union of everyone who owes or paid for the calculated cars.
        TreeSet<Long> users = new TreeSet<>();
        users.addAll(owedVar.keySet());
        users.addAll(costRepository.findDistinctBuyerIdsByYearAndCars(year, carIds));

        List<ExpensesUserYear> expenses = new ArrayList<>();
        List<CostDistributionLogYear> logs = new ArrayList<>();
        for (Long userId : users) {
            BigDecimal paidVar = money(costRepository
                    .sumPriceByUserYearTypeAndCars(userId, year, CostType.VARIABLE, carIds));
            BigDecimal paidFix = money(costRepository
                    .sumPriceByUserYearTypeAndCars(userId, year, CostType.FIXED, carIds));
            BigDecimal diffVar = paidVar.subtract(owedVar.getOrDefault(userId, ZERO_MONEY));
            BigDecimal diffFix = paidFix.subtract(owedFix.getOrDefault(userId, ZERO_MONEY));
            expenses.add(new ExpensesUserYear(year, userId, paidVar.add(paidFix)));
            logs.add(new CostDistributionLogYear(year, userId, diffVar, diffFix));
        }
        expensesRepository.saveAll(expenses);
        logRepository.saveAll(logs);
    }

    /**
     * Aggregates every month of the year that has drives but no persisted month total, so a
     * yearly run never silently drops a month the user forgot to aggregate. Months already
     * aggregated are left untouched — manual corrections survive.
     */
    private void aggregateMissingMonths(Long carId, Integer year) {
        List<DriveLogMonthTotal> rows = new ArrayList<>();
        distanceByMonthAndDriver(carId, year).forEach((month, distanceByDriver) -> {
            if (monthTotalRepository.existsByYearAndMonthAndCarId(year, month, carId)) {
                return;
            }
            distanceByDriver.forEach((userId, distance) ->
                    rows.add(new DriveLogMonthTotal(year, month, userId, carId, distance)));
        });
        monthTotalRepository.saveAll(rows);
    }

    /** Per-driver monthly distance from consecutive odometer readings (see the frontend rule). */
    private Map<Long, Integer> monthlyDistanceByDriver(Long carId, Integer year, Integer month) {
        return distanceByMonthAndDriver(carId, year).getOrDefault(month, Map.of());
    }

    /** One pass over the car's drives, bucketing each odometer delta by month and driver. */
    private Map<Integer, Map<Long, Integer>> distanceByMonthAndDriver(Long carId, Integer year) {
        Map<Integer, Map<Long, Integer>> byMonth = new LinkedHashMap<>();
        Integer previousOdometer = null;
        for (Drive drive : driveRepository.findByCarCarIdOrderByOdometerAscDriveDateAsc(carId)) {
            if (previousOdometer != null) {
                int delta = drive.getOdometer() - previousOdometer;
                if (delta > 0 && drive.getDriveDate().getYear() == year) {
                    byMonth.computeIfAbsent(drive.getDriveDate().getMonthValue(),
                                    m -> new LinkedHashMap<>())
                            .merge(drive.getDriver().getUserId(), delta, Integer::sum);
                }
            }
            previousOdometer = drive.getOdometer();
        }
        return byMonth;
    }

    /**
     * Allocates a money total across users in proportion to their weights, rounded to
     * cents, with the rounding remainder placed on the last user so the parts sum exactly
     * to {@code total}. Zero total weight yields an even split (remainder on the last).
     */
    private Map<Long, BigDecimal> allocate(BigDecimal total, List<Long> users,
            Map<Long, BigDecimal> weightByUser) {
        Map<Long, BigDecimal> out = new LinkedHashMap<>();
        if (users.isEmpty()) {
            return out;
        }
        BigDecimal amount = money(total);
        BigDecimal totalWeight = users.stream()
                .map(u -> weightByUser.getOrDefault(u, BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal running = ZERO_MONEY;
        for (int i = 0; i < users.size(); i++) {
            Long userId = users.get(i);
            BigDecimal share;
            if (i == users.size() - 1) {
                share = amount.subtract(running);
            } else if (totalWeight.signum() == 0) {
                share = amount.divide(BigDecimal.valueOf(users.size()), 2, RoundingMode.HALF_UP);
            } else {
                share = amount.multiply(weightByUser.getOrDefault(userId, BigDecimal.ZERO))
                        .divide(totalWeight, 2, RoundingMode.HALF_UP);
            }
            out.put(userId, share);
            running = running.add(share);
        }
        return out;
    }

    /** Percentages (0..100, two decimals) proportional to weight, remainder on the last user. */
    private Map<Long, BigDecimal> percentagesByWeight(List<Long> users, Map<Long, BigDecimal> weightByUser) {
        return allocate(HUNDRED.setScale(2), users, weightByUser);
    }

    /** Even percentages summing to 100.00, remainder on the last user (e.g. 33.33/33.33/33.34). */
    private Map<Long, BigDecimal> equalPercentages(List<Long> users) {
        Map<Long, BigDecimal> equalWeights = new LinkedHashMap<>();
        users.forEach(u -> equalWeights.put(u, BigDecimal.ONE));
        return allocate(HUNDRED.setScale(2), users, equalWeights);
    }

    private Map<Long, String> userNames() {
        Map<Long, String> names = new LinkedHashMap<>();
        for (User u : userRepository.findAll()) {
            names.put(u.getUserId(), (u.getFirstname() + " " + u.getLastname()).trim());
        }
        return names;
    }

    private void requireCar(Long carId) {
        if (!carRepository.existsById(carId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found");
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }
}
