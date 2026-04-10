package de.digidrivelog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.Cost;

@Repository
public interface CostRepository extends JpaRepository<Cost, Long> {
	java.util.List<Cost> findByCarCarId(Long carId);
	java.util.List<Cost> findByBuyerUserId(Long userId);
}