package de.digidrivelog.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.Drive;

@Repository
public interface DriveRepository extends JpaRepository<Drive, Long> {
	java.util.List<Drive> findByCarCarId(Long carId);
	java.util.List<Drive> findByDriverUserId(Long userId);
	java.util.List<Drive> findByCarCarIdAndDriverUserId(Long carId, Long userId);

	@Query("""
			SELECT d.driveId AS driveId,
			       (d.currentMileage - (
			           SELECT MAX(d2.currentMileage)
			           FROM Drive d2
			           WHERE d2.car.carId = d.car.carId
			             AND d2.currentMileage < d.currentMileage
			       )) AS drivenDistance
			FROM Drive d
			WHERE d.car.carId = :carId
			""")
	java.util.List<DriveDistanceProjection> findDrivenDistanceByCarId(@Param("carId") Long carId);

	@Query("""
			SELECT d.driveId AS driveId,
			       (d.currentMileage - (
			           SELECT MAX(d2.currentMileage)
			           FROM Drive d2
			           WHERE d2.car.carId = d.car.carId
			             AND d2.currentMileage < d.currentMileage
			       )) AS drivenDistance
			FROM Drive d
			WHERE d.driver.userId = :userId
			""")
	java.util.List<DriveDistanceProjection> findDrivenDistanceByUserId(@Param("userId") Long userId);

	@Query("""
			SELECT d.driveId AS driveId,
			       (d.currentMileage - (
			           SELECT MAX(d2.currentMileage)
			           FROM Drive d2
			           WHERE d2.car.carId = d.car.carId
			             AND d2.currentMileage < d.currentMileage
			       )) AS drivenDistance
			FROM Drive d
			WHERE d.driveId = :driveId
			""")
	DriveDistanceProjection findDrivenDistanceByDriveId(@Param("driveId") Long driveId);
}
