package org.chomookun.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.execution.model.Execution;
import org.chomookun.fintics.client.asset.AssetClient;
import org.chomookun.fintics.model.Asset;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class AssetCollectorTest extends CoreTestSupport {

    private final AssetCollector assetCollector;

    private final AssetClient assetClient;

    @PersistenceContext
    private final EntityManager entityManager;

    @Disabled
    @Test
    void extractPrimaryAssetIds() {
        // given
        List<Asset> assets = assetClient.getAssets();
        // when
        List<String> primaryAssetIds = assetCollector.extractPrimaryAssetIds(assets);
        // then
        log.info("primaryAssetIds: {}", primaryAssetIds);
    }

    @Disabled
    @Test
    void sortByPrimaryAssetIds() {
        // given
        List<Asset> assets = assetClient.getAssets();
        List<String> primaryAssetIds = List.of("US.SPY", "US.TLT");
        // when
        assetCollector.sortByPrimaryAssetIds(assets, primaryAssetIds);
        // then
        log.info("assets: {}", assets);
        assertEquals(primaryAssetIds.get(0), assets.get(0).getAssetId());
        assertEquals(primaryAssetIds.get(1), assets.get(1).getAssetId());
    }

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
        Execution execution = Execution.builder()
                .executionId("test")
                .taskName("test task")
                .build();
        // when
        assetCollector.saveAssets(execution);
        // then
        List<AssetEntity> brokerAssetEntities = entityManager
                .createQuery("select a from AssetEntity a", AssetEntity.class)
                .getResultList();
        assertTrue(brokerAssetEntities.size() > 0);
    }

}