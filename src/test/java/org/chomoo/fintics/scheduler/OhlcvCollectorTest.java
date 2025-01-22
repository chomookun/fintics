package org.chomoo.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomoo.fintics.scheduler.OhlcvCollector;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomoo.arch4j.core.common.test.CoreTestSupport;
import org.chomoo.fintics.FinticsConfiguration;
import org.chomoo.fintics.dao.OhlcvEntity;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class OhlcvCollectorTest extends CoreTestSupport {

    private final OhlcvCollector ohlcvCollector;

    @Disabled
    @Test
    void collect() {
        // given
        // when
        ohlcvCollector.collect();

        // then
        List<OhlcvEntity> assetOhlcvEntities = entityManager
                .createQuery("select a from OhlcvEntity a", OhlcvEntity.class)
                .setMaxResults(100)
                .getResultList();
        log.info("ohlcvEntities:{}", assetOhlcvEntities);
        assertTrue(assetOhlcvEntities.size() > 0);
    }


}