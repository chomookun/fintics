package org.chomookun.fintics.core.broker.client;

import lombok.Getter;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.broker.model.Balance;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.profit.model.DividendProfit;
import org.chomookun.fintics.core.profit.model.RealizedProfit;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

/**
 * broker client interface
 */
public abstract class BrokerClient {

    @Getter
    private final BrokerClientDefinition definition;

    @Getter
    private final Properties properties;

    /**
     * constructor
     * @param definition definition
     * @param properties properties
     */
    public BrokerClient(BrokerClientDefinition definition, Properties properties) {
        this.definition = definition;
        this.properties = properties;
    }

    /**
     * check open datetime
     * @param datetime datetime
     * @return is opened
     */
    public abstract boolean isOpened(LocalDateTime datetime) throws InterruptedException;

    /**
     * returns minute ohlcvs
     * @param asset asset
     * @return minute ohlcvs
     */
    public abstract List<Ohlcv> getMinuteOhlcvs(Asset asset) throws InterruptedException;

    /**
     * returns daily ohlcvs
     * @param asset asset
     * @return daily ohlcvs
     */
    public abstract List<Ohlcv> getDailyOhlcvs(Asset asset) throws InterruptedException;

    /**
     * returns order book
     * @param asset asset
     * @return order book
     */
    public abstract OrderBook getOrderBook(Asset asset) throws InterruptedException;

    /**
     * check available price and quantity
     * @param price price
     * @param quantity quantity
     * @return whether price and quantity is available or not
     */
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

    /**
     * returns quantity scale
     * @return quantity scale
     */
    public int getQuantityScale() {
        return 0;
    }

    /**
     * returns balance
     * @return balance
     */
    public abstract Balance getBalance() throws InterruptedException;

    /**
     * submits order
     * @param asset asset
     * @param order order
     * @return submitted order
     */
    public abstract Order submitOrder(Asset asset, Order order) throws InterruptedException;

    /**
     * returns waiting orders
     * @return list of waiting order
     */
    public abstract List<Order> getWaitingOrders() throws InterruptedException;

    /**
     * amends order
     * @param asset asset
     * @param order order
     * @return amended order
     */
    public abstract Order amendOrder(Asset asset, Order order) throws InterruptedException;

    /**
     * returns realized profits
     * @param dateFrom date from
     * @param dateTo  date to
     * @return realized profits
     */
    public abstract List<RealizedProfit> getRealizedProfits(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException;

    /**
     * returns dividend histories
     * @param dateFrom date from
     * @param dateTo date to
     * @return dividend histories
     */
    public abstract List<DividendProfit> getDividendProfits(LocalDate dateFrom, LocalDate dateTo) throws InterruptedException;

    /**
     * convert to asset id
     * @param symbol symbol
     * @return asset id
     */
    public final String toAssetId(String symbol) {
        return String.format("%s.%s", this.definition.getMarket(), symbol);
    }

}
