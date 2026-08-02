package de.digidrivelog.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.CostTotalCarYear;

@Repository
public interface CostTotalCarYearRepository
        extends JpaRepository<CostTotalCarYear, CostTotalCarYear.Id> {

    boolean existsByYearAndCarId(Integer year, Long carId);

    /** The cars that have a completed yearly calculation for the year (set S). */
    List<CostTotalCarYear> findByYear(Integer year);

    /** The years already calculated for one car. */
    List<CostTotalCarYear> findByCarId(Long carId);

    void deleteByYearAndCarId(Integer year, Long carId);
}
