package org.chomookun.fintics.daemon.ohlcv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.daemon.FinticsDaemonConfiguration;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.ohlcv.entity.OhlcvEntity;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsDaemonConfiguration.class)
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