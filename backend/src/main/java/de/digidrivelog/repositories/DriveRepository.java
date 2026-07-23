package de.digidrivelog.repositories;

import java.util.List;
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

	/**
	 * All drives for a car in physical odometer order. Consecutive readings give
	 * per-drive distances (this reading minus the previous), which the monthly
	 * aggregation buckets by the current drive's month.
	 */
	List<Drive> findByCarCarIdOrderByOdometerAscDriveDateAsc(Long carId);
}
