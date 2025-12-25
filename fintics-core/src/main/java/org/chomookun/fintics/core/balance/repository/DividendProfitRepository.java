package org.chomookun.fintics.core.balance.repository;

import org.chomookun.fintics.core.balance.entity.BalanceHistoryEntity;
import org.chomookun.fintics.core.balance.entity.DividendProfitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DividendProfitRepository extends JpaRepository<DividendProfitEntity, DividendProfitEntity.Pk> {

    @Query("""
        select max(a.date)
        from DividendProfitEntity a
        where a.brokerId = :brokerId
        """)
    Optional<LocalDate> findLastDateByBrokerId(@Param("brokerId") String brokerId);

    @Query("""
        select a from DividendProfitEntity a
        where a.brokerId = :brokerId
        and (:dateFrom is null or a.date >= :dateFrom)
        and (:dateTo is null or a.date <= :dateTo)
        order by a.date desc
        """)
    List<DividendProfitEntity> findAllByBrokerId(
            @Param("brokerId") String brokerId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

}
