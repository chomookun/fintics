package org.chomookun.fintics.core.trade.repository;

import org.chomookun.fintics.core.trade.entity.TradeAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeAssetRepository extends JpaRepository<TradeAssetEntity, TradeAssetEntity.Pk> {

    @Query("select a from TradeAssetEntity a where a.tradeId = :tradeId")
    List<TradeAssetEntity> findAllByTradeId(@Param("tradeId") String tradeId);

    @Modifying
    @Query("delete from TradeAssetEntity a where a.tradeId = :tradeId")
    void deleteByTradeId(@Param("tradeId") String tradeId);

}
