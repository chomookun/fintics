package org.chomookun.fintics.core.broker.client;

import lombok.Getter;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.balance.model.Balance;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.balance.model.DividendProfit;
import org.chomookun.fintics.core.balance.model.RealizedProfit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

public abstract class BrokerClient {

    @Getter
    private final BrokerClientDefinition definition;

    @Getter
    private final Properties properties;

    public BrokerClient(BrokerClientDefinition definition, Properties properties) {
        this.definition = definition;
        this.properties = properties;
    }

    public abstract boolean isOpened(LocalDateTime datetime) throws InterruptedException;

    public abstract boolean isOhlcvTrusted() throws InterruptedException;

    public abstract List<Ohlcv> getMinuteOhlcvs(Asset asset) throws InterruptedException;

    public abstract List<Ohlcv> getDailyOhlcvs(Asset asset) throws InterruptedException;

    public abstract OrderBook getOrderBook(Asset asset) throws InterruptedException;

    public boolean isAvailablePriceAndQuantity(BigDecimal price, BigDecimal quantity) {
        boolean available = true;
        if (price.compareTo(BigDecimal.ZERO) <= 0) {
            available = false;
        }
        if (quantity.compareTo(BigDecimal.ONE) < 0) {
            available = false;
        }
        return available;
    }

    public int getQuantityScale() {
        return 0;
    }

    public abstract Balance getBalance() throws InterruptedException;

    public abstract Order submitOrder(Asset asset, Order order) throws InterruptedException;

    public abstract List<Order> getWaitingOrders() throws InterruptedException;

    public abstract Order amendOrder(Asset asset, Order order) throws InterruptedException;

    public abstract List<RealizedProfit> getRealizedProfits(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException;

    public abstract List<DividendProfit> getDividendProfits(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException;

    public final String toAssetId(String symbol) {
        return String.format("%s.%s", this.definition.getMarket(), symbol);
    }

}
