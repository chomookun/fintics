package org.oopscraft.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.dao.BasketAssetEntity;
import org.oopscraft.fintics.dao.BasketEntity;
import org.oopscraft.fintics.dao.OhlcvEntity;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
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