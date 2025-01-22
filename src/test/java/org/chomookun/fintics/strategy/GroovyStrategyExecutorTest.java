package org.chomookun.fintics.strategy;

import com.github.javaparser.utils.LineSeparator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.chomookun.fintics.model.*;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
class GroovyStrategyExecutorTest {

    Trade getTestTrade() {
        return Trade.builder()
                .tradeId("test")
                .build();
    }

    OrderBook getTestOrderBook() {
        return OrderBook.builder()
                .price(BigDecimal.valueOf(10000))
                .bidPrice(BigDecimal.valueOf(9990))
                .askPrice(BigDecimal.valueOf(10010))
                .build();
    }

    TradeAsset getTestTradeAsset() {
        return TradeAsset.builder()
                .minuteOhlcvs(IntStream.range(1,501)
                        .mapToObj(i -> {
                            BigDecimal price = BigDecimal.valueOf(1000 - (i*10));
                            return Ohlcv.builder()
                                    .dateTime(LocalDateTime.now().minusMinutes(i))
                                    .open(price)
                                    .high(price)
                                    .low(price)
                                    .close(price)
                                    .volume(BigDecimal.valueOf(100))
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .dailyOhlcvs(IntStream.range(1,301)
                        .mapToObj(i -> {
                            BigDecimal price = BigDecimal.valueOf(1000 - (i*10));
                            return Ohlcv.builder()
                                    .dateTime(LocalDateTime.now().minusMinutes(i))
                                    .open(price)
                                    .high(price)
                                    .low(price)
                                    .close(price)
                                    .volume(BigDecimal.valueOf(10000))
                                    .build();
                        })
                        .collect(Collectors.toList()))
                .build();
    }

    String loadGroovyFileAsString(String fileName) {
        String filePath = null;
        try {
            filePath = new File(".").getCanonicalPath() + "/src/main/groovy/" + fileName;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        StringBuilder stringBuilder = new StringBuilder();
        try (InputStream inputStream = new FileInputStream(new File(filePath))) {
            IOUtils.readLines(inputStream, StandardCharsets.UTF_8).forEach(line -> {
                stringBuilder.append(line).append(LineSeparator.LF);
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return stringBuilder.toString();
    }

    @Test
    void test() {
        // given
        Trade trade = getTestTrade();
        TradeAsset tradeAsset = getTestTradeAsset();
        OrderBook orderBook = getTestOrderBook();
        trade.setStrategyVariables("");
        Strategy strategy = Strategy.builder()
                .script("return null")
                .build();

        // when
        StrategyRunner strategyExecutor = GroovyStrategyRunner.builder()
                .tradeAsset(tradeAsset)
                .strategy(strategy)
                .dateTime(LocalDateTime.now())
                .orderBook(orderBook)
                .build();
        StrategyResult strategyResult = strategyExecutor.run();

        // then
        log.info("== strategyResult:{}", strategyResult);
    }

}