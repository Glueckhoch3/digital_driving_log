package de.digidrivelog.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.digidrivelog.dto.calculation.ConsumptionDto;
import de.digidrivelog.services.ConsumptionService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ddl/api")
@RequiredArgsConstructor
public class ConsumptionController {
    private final ConsumptionService consumptionService;

    @GetMapping("/vehicles/{carId}/consumption")
    public ResponseEntity<ConsumptionDto> getConsumption(@PathVariable Long carId,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(consumptionService.getConsumption(carId, year));
    }
}
