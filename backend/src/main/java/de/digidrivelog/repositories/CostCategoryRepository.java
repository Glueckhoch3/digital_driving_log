package de.digidrivelog.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.CostCategory;

@Repository
public interface CostCategoryRepository extends JpaRepository<CostCategory, Long> {
    Optional<CostCategory> findByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndCostCategoryIdNot(String name, Long costCategoryId);
}
