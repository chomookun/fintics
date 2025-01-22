package org.chomookun.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.dao.AssetEntity;
import org.chomookun.fintics.dao.TradeEntity;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
class AssetCollectorTest extends CoreTestSupport {

    private final AssetCollector assetCollector;

    @PersistenceContext
    private final EntityManager entityManager;

    @Disabled
    @Test
    void collect() {
        // given
        TradeEntity tradeEntity = TradeEntity.builder()
                .tradeId("test")
                .enabled(true)
                .build();
        entityManager.persist(tradeEntity);
        entityManager.flush();

        // when
        assetCollector.collect();

        // then
        List<AssetEntity> brokerAssetEntities = entityManager
                .createQuery("select a from AssetEntity a", AssetEntity.class)
                .getResultList();
        assertTrue(brokerAssetEntities.size() > 0);
    }

    @Disabled
    @Test
    void saveAssets() {
        // given
        // when
        assetCollector.saveAssets();
        // then
        List<AssetEntity> brokerAssetEntities = entityManager
                .createQuery("select a from AssetEntity a", AssetEntity.class)
                .getResultList();
        assertTrue(brokerAssetEntities.size() > 0);
    }

}