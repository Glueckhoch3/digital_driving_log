package de.digidrivelog.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.digidrivelog.dto.calculation.ConsumptionDto;
import de.digidrivelog.dto.calculation.ConsumptionIntervalDto;
import de.digidrivelog.models.Cost;
import de.digidrivelog.repositories.CarRepository;
import de.digidrivelog.repositories.CostRepository;
import lombok.RequiredArgsConstructor;

/**
 * Fuel consumption from fill-ups with odometer readings (full-tank assumption):
 * for each fill-up after the first, litres / distance-since-previous-fill-up * 100.
 * The first fill-up is only the baseline. A year filter applies to the date of the
 * later fill-up, so the baseline may come from the previous year.
 */
@Service
@RequiredArgsConstructor
public class ConsumptionService {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private final CostRepository costRepository;
    private final CarRepository carRepository;

    @Transactional(readOnly = true)
    public ConsumptionDto getConsumption(Long carId, Integer year) {
        if (!carRepository.existsById(carId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Car not found");
        }
        List<Cost> fills = costRepository.findFuelWithOdometerByCar(carId);
        List<ConsumptionIntervalDto> intervals = new ArrayList<>();
        int totalKm = 0;
        BigDecimal totalLiters = BigDecimal.ZERO;

        for (int i = 1; i < fills.size(); i++) {
            Cost prev = fills.get(i - 1);
            Cost cur = fills.get(i);
            int km = cur.getOdometer() - prev.getOdometer();
            if (km <= 0) {
                continue; // identical readings carry no distance
            }
            if (year != null && cur.getDayOfTransaction().getYear() != year) {
                continue;
            }
            BigDecimal liters = cur.getQuantity();
            intervals.add(new ConsumptionIntervalDto(cur.getCostId(), cur.getDayOfTransaction(),
                    cur.getOdometer(), km, liters, cur.getPrice(), per100(liters, km)));
            totalKm += km;
            totalLiters = totalLiters.add(liters);
        }

        BigDecimal average = totalKm > 0 ? per100(totalLiters, totalKm) : null;
        return new ConsumptionDto(carId, year, intervals, average, totalKm, totalLiters);
    }

    private static BigDecimal per100(BigDecimal liters, int km) {
        return liters.multiply(HUNDRED).divide(BigDecimal.valueOf(km), 2, RoundingMode.HALF_UP);
    }
}
