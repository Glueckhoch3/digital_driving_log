package main.java.de.digidrivelog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/costs")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class CostController {
    private final CostService costService;

    @GetMapping
    public ResponseEntity<List<CostDto>> getAllCosts() {
        List<CostDto> costs = costService.getAllCosts();
        return ResponseEntity.ok(costs);    
    }

    @GetMapping("/{costId}")
    public ResponseEntity<CostDto> getCostById(@PathVariable Long costId) {
        CostDto cost = costService.getCostById(costId);
        return ResponseEntity.ok(cost);
    }

    @PutMapping("/{costId}")
    public ResponseEntity<CostDto> updateCost(@PathVariable Long costId, @Valid @RequestBody UpdateCostRequest request) {
        CostDto updatedCost = costService.updateCost(costId, request);
        return ResponseEntity.ok(updatedCost);
    }

    @DeleteMapping("/{costId}")
    public ResponseEntity<Void> deleteCost(@PathVariable Long costId) {
        costService.deleteCost(costId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/vehicles/{carId}")
    public  ResponseEntity<List<CostDto>> getAllCostsByVehicle(@PathVariable Long carId) {
        List<CostDto> costs = costService.getAllCostsByVehicle(carId);
        return ResponseEntity.ok(costs);    
    }

    @PostMapping("/vehicles/{carId}")
    public ResponseEntity<CostDto> createCost(@Valid @RequestBody CreateCostRequest request ) {
        CostDto createdCost = costService.createCost(request);
        return ResponseEntity.ok(createdCost);
    }
}