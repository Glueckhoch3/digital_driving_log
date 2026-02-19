package de.digidrivelog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.Car;

@Repository
public interface CarRepository extends JpaRepository<Car, Long> {
}