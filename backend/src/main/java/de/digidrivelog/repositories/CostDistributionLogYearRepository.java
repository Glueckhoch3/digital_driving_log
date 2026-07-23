package de.digidrivelog.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.CostDistributionLogYear;

@Repository
public interface CostDistributionLogYearRepository
        extends JpaRepository<CostDistributionLogYear, CostDistributionLogYear.Id> {

    List<CostDistributionLogYear> findByYear(Integer year);

    void deleteByYear(Integer year);
}
