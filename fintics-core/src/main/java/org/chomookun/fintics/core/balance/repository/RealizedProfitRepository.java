package org.chomookun.fintics.core.balance.repository;

import org.chomookun.fintics.core.balance.entity.BalanceHistoryEntity;
import org.chomookun.fintics.core.balance.entity.RealizedProfitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface RealizedProfitRepository extends JpaRepository<RealizedProfitEntity, RealizedProfitEntity.Pk> {

    @Query("""
        select max(a.date)
        from RealizedProfitEntity a
        where a.brokerId = :brokerId
        """)
    Optional<LocalDate> findLastDateByBrokerId(@Param("brokerId") String brokerId);

    /**
     * Find all by broker id
     * @param brokerId broker id
     * @param dateFrom date from
     * @param dateTo date to
     * @return list of realized profits
     */
    @Query("""
        select a from RealizedProfitEntity a
        where a.brokerId = :brokerId
        and a.date between :dateFrom and :dateTo
        order by a.date desc
        """)
    List<RealizedProfitEntity> findAllByBrokerId(
            @Param("brokerId") String brokerId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );
}
