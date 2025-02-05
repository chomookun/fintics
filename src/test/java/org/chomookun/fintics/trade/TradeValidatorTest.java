package org.chomookun.fintics.trade;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.model.Ohlcv;
import org.chomookun.fintics.model.OrderBook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class TradeValidatorTest {

    @Test
    void isValidOhlcvsWithCorrect() {
        // given
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<Ohlcv> ohlcvs = IntStream.range(0, 1000).mapToObj(i -> Ohlcv.builder()
                .dateTime(now.minusMinutes(i))
                .build()).collect(Collectors.toList());
        // when
        try {
            TradeValidator.validateOhlcvs(ohlcvs);
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    void isValidOhlcvsWithError() {
        // given
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        Random random = new Random();
        List<Ohlcv> ohlcvs = IntStream.range(0, 1000).mapToObj(i -> Ohlcv.builder()
                .dateTime(now.minusMinutes(i + random.nextInt(3)))
                .build()).collect(Collectors.toList());
        // when
        try {
            TradeValidator.validateOhlcvs(ohlcvs);
            fail();
        } catch (IllegalArgumentException e) {
            return;
        }
    }

    @Test
    void isValidOrderBookWithNormal() {
        // given
        OrderBook orderBook = OrderBook.builder()
                .price(BigDecimal.valueOf(1000))
                .tickPrice(BigDecimal.valueOf(10))
                .askPrice(BigDecimal.valueOf(1000))
                .bidPrice(BigDecimal.valueOf(990))
                .build();
        // when
        try {
            TradeValidator.validateOrderBook(orderBook);
            log.info("order book is valid");
        } catch (IllegalArgumentException e) {
            fail();
        }
    }

    @Test
    void isValidOrderBookWithErrorAskBidPrice() {
        // given
        OrderBook orderBook = OrderBook.builder()
                .price(BigDecimal.valueOf(1000))
                .tickPrice(BigDecimal.valueOf(10))
                .askPrice(BigDecimal.valueOf(1000))
                .bidPrice(BigDecimal.valueOf(1000))
                .build();
        // when
        try {
            TradeValidator.validateOrderBook(orderBook);
            fail();
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return;
        }
    }

    @Test
    void isValidOrderBookWithErrorTickPrice() {
        // given
        OrderBook orderBook = OrderBook.builder()
                .price(BigDecimal.valueOf(1000))
                .tickPrice(BigDecimal.valueOf(100))
                .askPrice(BigDecimal.valueOf(1000))
                .bidPrice(BigDecimal.valueOf(990))
                .build();
        // when
        try {
            TradeValidator.validateOrderBook(orderBook);
            fail();
        } catch (IllegalArgumentException e) {
            log.info(e.getMessage());
            return;
        }
    }

}