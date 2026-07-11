package de.digidrivelog.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.Cost;

@Repository
public interface CostRepository extends JpaRepository<Cost, Long> {
	Page<Cost> findByCarCarId(Long carId, Pageable pageable);
	Page<Cost> findByBuyerUserId(Long userId, Pageable pageable);
}
