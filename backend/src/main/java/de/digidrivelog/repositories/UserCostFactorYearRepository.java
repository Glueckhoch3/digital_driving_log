package de.digidrivelog.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.UserCostFactorYear;

@Repository
public interface UserCostFactorYearRepository
        extends JpaRepository<UserCostFactorYear, UserCostFactorYear.Id> {

    List<UserCostFactorYear> findByYearAndCarId(Integer year, Long carId);

    List<UserCostFactorYear> findByYear(Integer year);

    void deleteByYearAndCarId(Integer year, Long carId);
}
