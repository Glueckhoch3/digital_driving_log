package de.digidrivelog.repositories;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.ExpensesUserYear;

@Repository
public interface ExpensesUserYearRepository
        extends JpaRepository<ExpensesUserYear, ExpensesUserYear.Id> {

    List<ExpensesUserYear> findByYear(Integer year);

    void deleteByYear(Integer year);
}
