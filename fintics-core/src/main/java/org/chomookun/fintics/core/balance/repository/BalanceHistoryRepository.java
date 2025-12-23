package org.chomookun.fintics.core.balance.repository;

import org.chomookun.fintics.core.balance.entity.BalanceHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BalanceHistoryRepository extends JpaRepository<BalanceHistoryEntity, BalanceHistoryEntity.Pk> {

    /**
     * Find all by broker id
     * @param brokerId broker id
     * @param dateFrom date from
     * @param dateTo date to
     * @return list of balance history
     */
    @Query("""
        select a from BalanceHistoryEntity a
        where a.brokerId = :brokerId
        and a.date between :dateFrom and :dateTo
        order by a.date desc
        """)
    List<BalanceHistoryEntity> findAllByBrokerId(
            @Param("brokerId") String brokerId,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo
    );

}
