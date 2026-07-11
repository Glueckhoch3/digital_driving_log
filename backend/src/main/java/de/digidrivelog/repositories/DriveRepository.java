package de.digidrivelog.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.Drive;

@Repository
public interface DriveRepository extends JpaRepository<Drive, Long> {
	Page<Drive> findByCarCarId(Long carId, Pageable pageable);
	Page<Drive> findByDriverUserId(Long userId, Pageable pageable);
	Page<Drive> findByCarCarIdAndDriverUserId(Long carId, Long userId, Pageable pageable);
}
