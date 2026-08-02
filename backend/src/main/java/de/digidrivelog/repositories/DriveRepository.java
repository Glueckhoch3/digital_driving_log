package de.digidrivelog.repositories;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.Drive;

@Repository
public interface DriveRepository extends JpaRepository<Drive, Long> {
	Page<Drive> findByCarCarId(Long carId, Pageable pageable);
	Page<Drive> findByDriverUserId(Long userId, Pageable pageable);
	Page<Drive> findByCarCarIdAndDriverUserId(Long carId, Long userId, Pageable pageable);

	/** Distinct years a car has any drive logged in, newest first — a coarse "has raw data" signal. */
	@Query("select distinct year(d.driveDate) from Drive d where d.car.carId = :carId order by 1 desc")
	List<Integer> findDistinctYearsByCarId(@Param("carId") Long carId);

	/**
	 * All drives for a car in physical odometer order. Consecutive readings give
	 * per-drive distances (this reading minus the previous), which the monthly
	 * aggregation buckets by the current drive's month.
	 */
	List<Drive> findByCarCarIdOrderByOdometerAscDriveDateAsc(Long carId);
}
