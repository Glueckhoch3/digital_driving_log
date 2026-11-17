package de.digidrivelog.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.digidrivelog.dto.costcategory.CostCategoryDto;
import de.digidrivelog.dto.costcategory.CreateCostCategoryRequest;
import de.digidrivelog.dto.costcategory.UpdateCostCategoryRequest;
import de.digidrivelog.services.CostCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/ddl/api/cost-categories")
@RequiredArgsConstructor
public class CostCategoryController {
    private final CostCategoryService service;

    @GetMapping
    public ResponseEntity<List<CostCategoryDto>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }

    @PostMapping
    public ResponseEntity<CostCategoryDto> create(@Valid @RequestBody CreateCostCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping("/{costCategoryId}")
    public ResponseEntity<CostCategoryDto> getById(@PathVariable Long costCategoryId) {
        return ResponseEntity.ok(service.getById(costCategoryId));
    }

    @PutMapping("/{costCategoryId}")
    public ResponseEntity<CostCategoryDto> update(@PathVariable Long costCategoryId,
            @Valid @RequestBody UpdateCostCategoryRequest request) {
        return ResponseEntity.ok(service.update(costCategoryId, request));
    }

    @DeleteMapping("/{costCategoryId}")
    public ResponseEntity<Void> delete(@PathVariable Long costCategoryId) {
        service.delete(costCategoryId);
        return ResponseEntity.noContent().build();
    }
}
