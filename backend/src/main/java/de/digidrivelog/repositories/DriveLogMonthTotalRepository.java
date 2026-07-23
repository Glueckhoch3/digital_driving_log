package de.digidrivelog.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.DriveLogMonthTotal;

@Repository
public interface DriveLogMonthTotalRepository
        extends JpaRepository<DriveLogMonthTotal, DriveLogMonthTotal.Id> {

    boolean existsByYearAndMonthAndCarId(Integer year, Integer month, Long carId);

    List<DriveLogMonthTotal> findByYearAndCarIdOrderByMonthAsc(Integer year, Long carId);

    void deleteByYearAndMonthAndCarId(Integer year, Integer month, Long carId);

    void deleteByYearAndCarId(Integer year, Long carId);
}
