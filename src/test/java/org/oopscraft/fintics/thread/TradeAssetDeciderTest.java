package org.oopscraft.fintics.thread;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
class TradeAssetDeciderTest {

    @Test
    void test() {
        // given
        String holdCondition = "println assetIndicator.getMinuteMacds(60,120,40);";
        Trade trade = Trade.builder()
                .tradeId("test")
                .holdCondition(holdCondition)
                .build();
        TradeAsset tradeAsset = TradeAsset.builder()
                .tradeId("test")
                .name("Test")
                .build();
        AssetIndicator assetIndicator = AssetIndicator.builder()
                .symbol(tradeAsset.getSymbol())
                .name(tradeAsset.getName())
                .minuteOhlcvs(IntStream.range(1,501)
                        .mapToObj(i -> {
                            BigDecimal price = BigDecimal.valueOf(1000 - (i*10));
                            return Ohlcv.builder()
                                    .dateTime(LocalDateTime.now().minusMinutes(i))
                                    .openPrice(price)
                                    .highPrice(price)
                                    .lowPrice(price)
                                    .closePrice(price)
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .dailyOhlcvs(IntStream.range(1,301)
                        .mapToObj(i -> {
                            BigDecimal price = BigDecimal.valueOf(1000 - (i*10));
                            return Ohlcv.builder()
                                    .dateTime(LocalDateTime.now().minusDays(i))
                                    .openPrice(price)
                                    .highPrice(price)
                                    .lowPrice(price)
                                    .closePrice(price)
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();
        Market market = Market.builder()
                .build();

        // when
        TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                .holdCondition(trade.getHoldCondition())
                .logger(log)
                .dateTime(LocalDateTime.now())
                .assetIndicator(assetIndicator)
                .build();
        Instant groovyStart = Instant.now();
        Boolean result = tradeAssetDecider.execute();
        log.info("groovy duration:{}", Duration.between(groovyStart, Instant.now()));

        Instant javaStart = Instant.now();
        assetIndicator.getMinuteMacds(60,120,40);
        log.info("java duration:{}", Duration.between(javaStart, Instant.now()));

        log.info("== result:{}", result);
    }

}