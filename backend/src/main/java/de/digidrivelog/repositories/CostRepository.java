package de.digidrivelog.repositories;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import de.digidrivelog.models.Cost;
import de.digidrivelog.models.CostType;

@Repository
public interface CostRepository extends JpaRepository<Cost, Long> {
	Page<Cost> findByCarCarId(Long carId, Pageable pageable);
	Page<Cost> findByBuyerUserId(Long userId, Pageable pageable);

	/** Total of a car's costs of one type in a year — the pool a factor splits. */
	@Query("""
			select coalesce(sum(c.price), 0) from Cost c
			where c.car.carId = :carId and year(c.dayOfTransaction) = :year and c.costType = :type
			""")
	BigDecimal sumPriceByCarYearAndType(@Param("carId") Long carId,
			@Param("year") Integer year, @Param("type") CostType type);

	/** What one driver paid of a cost type in a year, limited to the calculated cars. */
	@Query("""
			select coalesce(sum(c.price), 0) from Cost c
			where c.buyer.userId = :userId and year(c.dayOfTransaction) = :year
			  and c.costType = :type and c.car.carId in :carIds
			""")
	BigDecimal sumPriceByUserYearTypeAndCars(@Param("userId") Long userId,
			@Param("year") Integer year, @Param("type") CostType type,
			@Param("carIds") Collection<Long> carIds);

	/** Distinct buyers who paid for any of the calculated cars in the year. */
	@Query("""
			select distinct c.buyer.userId from Cost c
			where year(c.dayOfTransaction) = :year and c.car.carId in :carIds
			""")
	List<Long> findDistinctBuyerIdsByYearAndCars(@Param("year") Integer year,
			@Param("carIds") Collection<Long> carIds);
}
