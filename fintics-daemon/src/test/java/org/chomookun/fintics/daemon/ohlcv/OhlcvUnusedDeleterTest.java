package org.chomookun.fintics.daemon.ohlcv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.daemon.FinticsDaemonConfiguration;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.basket.entity.BasketAssetEntity;
import org.chomookun.fintics.core.basket.entity.BasketEntity;
import org.chomookun.fintics.core.ohlcv.entity.OhlcvEntity;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsDaemonConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class OhlcvUnusedDeleterTest extends CoreTestSupport {

    private final OhlcvUnusedDeleter ohlcvUnusedDeleter;

    @Test
    void delete() {
        // given
        String usedAssetId = "usedAsset";
        String notUsedAssetId = "notUsedAsset";
        for (String assetId : List.of(usedAssetId, notUsedAssetId)) {
            entityManager.persist(OhlcvEntity.builder()
                    .assetId(assetId)
                    .dateTime(LocalDateTime.now())
                    .type(Ohlcv.Type.MINUTE)
                    .build());
        }
        BasketAssetEntity basketAssetEntity = BasketAssetEntity.builder()
                .basketId("test")
                .assetId(usedAssetId)
                .build();
        BasketEntity basketEntity = BasketEntity.builder()
                .basketId("test")
                .basketAssets(List.of(basketAssetEntity))
                .build();
        entityManager.persist(basketEntity);
        entityManager.flush();

        // when
        ohlcvUnusedDeleter.delete();

        // then
        List<OhlcvEntity> ohlcvEntities = entityManager
                .createQuery("select a from OhlcvEntity a", OhlcvEntity.class)
                .getResultList();
        log.info("ohlcvEntities:{}", ohlcvEntities);

        boolean existUsedOhlcv = ohlcvEntities.stream().anyMatch(it -> Objects.equals(it.getAssetId(), usedAssetId));
        boolean existUnusedOhlcv = ohlcvEntities.stream().anyMatch(it -> Objects.equals(it.getAssetId(), notUsedAssetId));
        assertTrue(existUsedOhlcv);
        assertFalse(existUnusedOhlcv);
    }

}