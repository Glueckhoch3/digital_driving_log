package de.digidrivelog.controller;

import de.digidrivelog.dto.calculation.CarAvailabilityDto;
import de.digidrivelog.dto.calculation.CombinedSettlementRowDto;
import de.digidrivelog.dto.calculation.FactorRowDto;
import de.digidrivelog.dto.calculation.MonthlyCalculationRequest;
import de.digidrivelog.dto.calculation.MonthlyDistanceDto;
import de.digidrivelog.dto.calculation.ParticipantSetDto;
import de.digidrivelog.dto.calculation.ParticipantUpdateRequest;
import de.digidrivelog.dto.calculation.YearlyCalculationRequest;
import de.digidrivelog.dto.calculation.YearlySettlementRowDto;
import de.digidrivelog.services.CalculationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ddl/api/calculations")
@RequiredArgsConstructor
public class CalculationController {

    private final CalculationService calculationService;

    // ----------------------------------------------------------------- actions

    @PostMapping("/monthly")
    public ResponseEntity<Void> aggregateMonth(@Valid @RequestBody MonthlyCalculationRequest request) {
        calculationService.aggregateMonth(request.getCarId(), request.getYear(), request.getMonth());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/yearly")
    public ResponseEntity<Void> calculateYear(@Valid @RequestBody YearlyCalculationRequest request) {
        calculationService.calculateYear(request.getCarId(), request.getYear());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/monthly")
    public ResponseEntity<Void> deleteMonth(@RequestParam Long carId,
            @RequestParam Integer year, @RequestParam Integer month) {
        calculationService.deleteMonth(carId, year, month);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/yearly")
    public ResponseEntity<Void> deleteYear(@RequestParam Long carId, @RequestParam Integer year) {
        calculationService.deleteYear(carId, year);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------ participants

    @GetMapping("/participants")
    public ResponseEntity<ParticipantSetDto> getParticipants(@RequestParam Long carId,
            @RequestParam Integer year) {
        return ResponseEntity.ok(calculationService.getParticipants(carId, year));
    }

    @PutMapping("/participants")
    public ResponseEntity<Void> saveParticipants(@Valid @RequestBody ParticipantUpdateRequest request) {
        calculationService.saveParticipants(request);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/participants")
    public ResponseEntity<Void> deleteParticipants(@RequestParam Long carId, @RequestParam Integer year) {
        calculationService.deleteParticipants(carId, year);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------ availability

    @GetMapping("/availability")
    public ResponseEntity<CarAvailabilityDto> availability(@RequestParam Long carId) {
        return ResponseEntity.ok(calculationService.getAvailability(carId));
    }

    // ------------------------------------------------------------------ checks

    @GetMapping("/monthly/exists")
    public ResponseEntity<Map<String, Boolean>> monthlyExists(@RequestParam Long carId,
            @RequestParam Integer year, @RequestParam Integer month) {
        return ResponseEntity.ok(Map.of("exists",
                calculationService.monthlyExists(carId, year, month)));
    }

    @GetMapping("/yearly/exists")
    public ResponseEntity<Map<String, Boolean>> yearlyExists(@RequestParam Long carId,
            @RequestParam Integer year) {
        return ResponseEntity.ok(Map.of("exists", calculationService.yearlyExists(carId, year)));
    }

    // ------------------------------------------------------------------- views

    @GetMapping("/monthly")
    public ResponseEntity<List<MonthlyDistanceDto>> monthlyDistances(@RequestParam Long carId,
            @RequestParam Integer year) {
        return ResponseEntity.ok(calculationService.getMonthlyDistances(carId, year));
    }

    @GetMapping("/yearly")
    public ResponseEntity<List<YearlySettlementRowDto>> yearlySettlement(@RequestParam Long carId,
            @RequestParam Integer year) {
        return ResponseEntity.ok(calculationService.getYearlySettlement(carId, year));
    }

    @GetMapping("/factors")
    public ResponseEntity<List<FactorRowDto>> factors(@RequestParam Long carId,
            @RequestParam Integer year) {
        return ResponseEntity.ok(calculationService.getFactors(carId, year));
    }

    @GetMapping("/combined")
    public ResponseEntity<List<CombinedSettlementRowDto>> combined(@RequestParam Integer year) {
        return ResponseEntity.ok(calculationService.getCombined(year));
    }
}
