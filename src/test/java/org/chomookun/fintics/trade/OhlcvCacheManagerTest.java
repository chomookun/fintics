package org.chomookun.fintics.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.model.Ohlcv;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class OhlcvCacheManagerTest extends CoreTestSupport {

    private final OhlcvCacheManager ohlcvCacheManager;

    @Test
    void getDailyOhlcvs() {
        // given
        String assetId = "US.SPY";
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusMonths(3);
        LocalDateTime dateTimeTo = LocalDateTime.now();
        // when
        List<Ohlcv> ohlcvs = ohlcvCacheManager.getDailyOhlcvs(assetId, dateTimeFrom, dateTimeTo);
        // then
        log.info("ohlcvs: {}", ohlcvs);
    }

    @Test
    void getMinuteOhlcvs() {
        // given
        String assetId = "US.SPY";
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(7);
        LocalDateTime dateTimeTo = LocalDateTime.now();
        // when
        List<Ohlcv> ohlcvs = ohlcvCacheManager.getMinuteOhlcvs(assetId, dateTimeFrom, dateTimeTo);
        // then
        log.info("ohlcvs: {}", ohlcvs);
    }

}