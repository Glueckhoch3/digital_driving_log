package de.digidrivelog.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import de.digidrivelog.dto.cost.*;
import de.digidrivelog.services.CostService;

@RestController
@RequestMapping("/ddl/api")
@RequiredArgsConstructor
public class CostController {
    private final CostService costService;

    @GetMapping("/costs")
    public ResponseEntity<Page<CostDto>> getAllCosts(
            @PageableDefault(size = 50, sort = "dayOfTransaction", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(costService.getAllCosts(pageable));
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
    public ResponseEntity<Page<CostDto>> getAllCostsByVehicle(
            @PathVariable Long carId,
            @PageableDefault(size = 50, sort = "dayOfTransaction", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(costService.getAllCostsByVehicle(carId, pageable));
    }

    @GetMapping("/users/{userId}/costs")
    public ResponseEntity<Page<CostDto>> getAllCostsByUser(
            @PathVariable Long userId,
            @PageableDefault(size = 50, sort = "dayOfTransaction", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(costService.getAllCostsByUser(userId, pageable));
    }
}
