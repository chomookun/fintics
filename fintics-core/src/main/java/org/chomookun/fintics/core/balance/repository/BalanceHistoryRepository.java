package org.chomookun.fintics.core.balance.repository;

import jakarta.validation.constraints.NotNull;
import org.chomookun.fintics.core.balance.entity.BalanceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BalanceHistoryRepository extends JpaRepository<BalanceHistoryEntity, BalanceHistoryEntity.Pk> {

    @Query("""
        select a from BalanceHistoryEntity a
        where a.brokerId = :brokerId
        and (:dateFrom is null or a.date >= :dateFrom)
        and (:dateTo is null or a.date <= :dateTo)
        order by a.date desc
        """)
    List<BalanceHistoryEntity> findAllByBrokerId(
            @Param("brokerId") String brokerId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

}
