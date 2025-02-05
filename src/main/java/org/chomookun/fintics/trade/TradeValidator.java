package org.chomookun.fintics.trade;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.model.Ohlcv;
import org.chomookun.fintics.model.OrderBook;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class TradeValidator {

    /**
     * Checks validity of ohlcvs
     * @param ohlcvs ohlcvs
     */
    public static void validateOhlcvs(List<Ohlcv> ohlcvs) {
        // checks date time order
        for (int i = 0; i < ohlcvs.size() - 1; i ++) {
            Ohlcv ohlcv = ohlcvs.get(i);
            Ohlcv nextOhlcv = ohlcvs.get(i + 1);
            if (!ohlcv.getDateTime().isAfter(nextOhlcv.getDateTime())) {
                throw new IllegalArgumentException("Ohlcvs dateTime order is invalid");
            }
        }
    }

    /**
     * Checks validity of order book
     * @param orderBook order book
     */
    public static void validateOrderBook(OrderBook orderBook) {
        // checks null
        Objects.requireNonNull(orderBook.getPrice(), "price is required");
        Objects.requireNonNull(orderBook.getTickPrice(), "tickPrice is required");
        Objects.requireNonNull(orderBook.getAskPrice(), "askPrice is required");
        Objects.requireNonNull(orderBook.getBidPrice(), "bidPrice is required");

        // compares bid, ask price
        if (orderBook.getBidPrice().compareTo(orderBook.getAskPrice()) >= 0) {
            throw new IllegalArgumentException(String.format("The bidPrice[%s] is greater than the askPrice[%s].", orderBook.getBidPrice(), orderBook.getAskPrice()));
        }

        // checks tick price
        BigDecimal gapPrice = orderBook.getAskPrice().subtract(orderBook.getBidPrice());
        if (orderBook.getTickPrice().compareTo(gapPrice) > 0) {
            throw new IllegalArgumentException(String.format("The tickPrice[%s] is greater than the gapPrice[%s].", orderBook.getTickPrice(), gapPrice));
        }
    }

}
