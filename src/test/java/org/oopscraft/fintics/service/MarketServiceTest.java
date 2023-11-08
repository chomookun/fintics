package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.Market;
import org.oopscraft.fintics.model.MarketIndicator;
import org.oopscraft.fintics.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class MarketServiceTest extends CoreTestSupport {

    private final MarketService marketService;

    @Disabled
    @Test
    void getMarket() throws InterruptedException {
        // given
        // when
        Market market = marketService.getMarket();

        // then
        assertNotNull(market);
    }

}