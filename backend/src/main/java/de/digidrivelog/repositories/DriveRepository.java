package de.digidrivelog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.Drive;

@Repository
public interface DriveRepository extends JpaRepository<Drive, Long> {
	java.util.List<Drive> findByCarCarId(Long carId);
	java.util.List<Drive> findByDriverUserId(Long userId);
	java.util.List<Drive> findByCarCarIdAndDriverUserId(Long carId, Long userId);
}