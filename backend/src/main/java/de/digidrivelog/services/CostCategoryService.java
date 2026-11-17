package de.digidrivelog.services;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import de.digidrivelog.dto.costcategory.CostCategoryDto;
import de.digidrivelog.dto.costcategory.CreateCostCategoryRequest;
import de.digidrivelog.dto.costcategory.UpdateCostCategoryRequest;
import de.digidrivelog.mappers.CostCategoryMapper;
import de.digidrivelog.models.CostCategory;
import de.digidrivelog.repositories.CostCategoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CostCategoryService {

    private final CostCategoryRepository repository;

    @Transactional(readOnly = true)
    public List<CostCategoryDto> getAll() {
        return repository.findAll(Sort.by("name")).stream().map(CostCategoryMapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public CostCategoryDto getById(Long id) {
        return CostCategoryMapper.toDto(require(id));
    }

    @Transactional
    public CostCategoryDto create(CreateCostCategoryRequest request) {
        if (repository.existsByNameIgnoreCase(request.getName().trim())) {
            throw nameConflict();
        }
        return CostCategoryMapper.toDto(repository.save(CostCategoryMapper.fromCreate(request)));
    }

    @Transactional
    public CostCategoryDto update(Long id, UpdateCostCategoryRequest request) {
        CostCategory existing = require(id);
        if (repository.existsByNameIgnoreCaseAndCostCategoryIdNot(request.getName().trim(), id)) {
            throw nameConflict();
        }
        CostCategoryMapper.applyUpdate(request, existing);
        return CostCategoryMapper.toDto(repository.save(existing));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost category not found");
        }
        try {
            repository.deleteById(id);
            // Flush so an FK violation surfaces here and is translated to 409.
            repository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cost category cannot be deleted because costs use it");
        }
    }

    private CostCategory require(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cost category not found"));
    }

    private ResponseStatusException nameConflict() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Cost category name already in use");
    }
}
