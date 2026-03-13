package main.java.de.digidrivelog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/costs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CostController {
    private final CostService costService;

    @GetMapping
    public ResponseEntity<List<CostDto>> getAllCosts() {
        List<CostDto> costs = costService.getAllCosts();
        return ResponseEntity.ok(costs);    
    }

    @GetMapping("/{id}")
    public ResponseEntity<CostDto> getCostById(@PathVariable Long id) {
        CostDto cost = costService.getCostById(id);
        return ResponseEntity.ok(cost);
    }

    @PostMapping
    public ResponseEntity<CostDto> createCost(@Valid @RequestBody CreateCostRequest request ) {
        CostDto createdCost = costService.createCost(request);
        return ResponseEntity.ok(createdCost);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CostDto> updateCost(@PathVariable Long id, @Valid @RequestBody UpdateCostRequest request) {
        CostDto updatedCost = costService.updateCost(id, request);
        return ResponseEntity.ok(updatedCost);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCost(@PathVariable Long id) {
        costService.deleteCost(id);
        return ResponseEntity.noContent().build();
    }
}