package de.digidrivelog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

import de.digidrivelog.dto.cost.*;
import de.digidrivelog.services.CostService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CostController {
    private final CostService costService;

    @GetMapping("/costs")
    public ResponseEntity<List<CostDto>> getAllCosts() {
        List<CostDto> costs = costService.getAllCosts();
        return ResponseEntity.ok(costs);
    }

    @PostMapping("/costs")
    public ResponseEntity<CostDto> createCost(@Valid @RequestBody CreateCostRequest request) {
        CostDto createdCost = costService.createCost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCost);
    }

    @GetMapping("/costs/{costId}")
    public ResponseEntity<CostDto> getCostById(@PathVariable Long costId) {
        CostDto cost = costService.getCostById(costId);
        return ResponseEntity.ok(cost);
    }

    @PutMapping("/costs/{costId}")
    public ResponseEntity<CostDto> updateCost(@PathVariable Long costId, @Valid @RequestBody UpdateCostRequest request) {
        CostDto updatedCost = costService.updateCost(costId, request);
        return ResponseEntity.ok(updatedCost);
    }

    @DeleteMapping("/costs/{costId}")
    public ResponseEntity<Void> deleteCost(@PathVariable Long costId) {
        costService.deleteCost(costId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicles/{carId}/costs")
    public  ResponseEntity<List<CostDto>> getAllCostsByVehicle(@PathVariable Long carId) {
        List<CostDto> costs = costService.getAllCostsByVehicle(carId);
        return ResponseEntity.ok(costs);
    }

    @GetMapping("/users/{userId}/costs")
    public ResponseEntity<List<CostDto>> getAllCostsByUser(@PathVariable Long userId) {
        List<CostDto> costs = costService.getAllCostsByUser(userId);
        return ResponseEntity.ok(costs);
    }
}