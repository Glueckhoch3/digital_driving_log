package de.digidrivelog.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.DriveAccountYear;

@Repository
public interface DriveAccountYearRepository
        extends JpaRepository<DriveAccountYear, DriveAccountYear.Id> {

    List<DriveAccountYear> findByYearAndCarId(Integer year, Long carId);

    void deleteByYearAndCarId(Integer year, Long carId);
}
