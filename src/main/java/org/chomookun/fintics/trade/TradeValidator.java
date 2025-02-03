package org.chomookun.fintics.trade;

import org.chomookun.fintics.model.Ohlcv;
import org.chomookun.fintics.model.OrderBook;

import java.util.List;

public class TradeValidator {

    /**
     * Checks validity of ohlcvs
     * @param ohlcvs ohlcvs
     * @return true if valid
     */
    public static void validateOhlcvs(List<Ohlcv> ohlcvs) {
        // checks order
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
     * @param ohlcv ohlcv
     */
    public static void validateOrderBook(OrderBook orderBook) {
        // compares bid, ask price
        if (orderBook.getBidPrice().compareTo(orderBook.getAskPrice()) >= 0) {
            throw new IllegalArgumentException("bid, ask price is invalid");
        }
    }

}
