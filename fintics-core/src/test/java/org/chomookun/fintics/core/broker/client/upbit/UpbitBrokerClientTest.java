package org.chomookun.fintics.core.broker.client.upbit;

import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.broker.model.Balance;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

@Slf4j
class UpbitBrokerClientTest {

    private static final String accessKey;

    private static final String secretKey;

    static {
        accessKey = System.getenv("UPBIT_ACCESS_KEY");
        secretKey = System.getenv("UPBIT_SECRET_KEY");
    }

    UpbitBrokerClient getUpbitTradeClient() {
        Properties properties = new Properties();
        properties.setProperty("accessKey", accessKey);
        properties.setProperty("secretKey", secretKey);
        return new UpbitBrokerClient(new UpbitBrokerClientDefinition(), properties);
    }

    Asset getTestAsset() {
        return Asset.builder()
                .assetId("UPBIT.KRW-BTC")
                .name("Bitcoin")
                .build();
    }

    @Tag("manual")
    @Test
    void getOrderBook() throws Exception {
        // given
        Asset tradeAsset = getTestAsset();
        // when
        OrderBook orderBook = getUpbitTradeClient().getOrderBook(tradeAsset);
        // then
        log.info("orderBook:{}", orderBook);
    }

    @Tag("manual")
    @Test
    void getMinuteOhlcvs() throws Exception {
        // given
        Asset tradeAsset = getTestAsset();
        LocalDateTime fromDateTime = LocalDateTime.now().minusWeeks(1);
        LocalDateTime toDateTime = LocalDateTime.now();
        // when
        List<Ohlcv> minuteOhlcvs = getUpbitTradeClient().getMinuteOhlcvs(tradeAsset);
        // then
        log.info("minuteOhlcvs:{}", minuteOhlcvs);
    }

    @Tag("manual")
    @Test
    void getDailyOhlcvs() throws Exception {
        // given
        Asset tradeAsset = getTestAsset();
        LocalDate fromDate = LocalDate.now().minusYears(1);
        LocalDate toDate = LocalDate.now();
        // when
        List<Ohlcv> dailyOhlcvs = getUpbitTradeClient().getDailyOhlcvs(tradeAsset);
        // then
        log.info("dailyOhlcvs:{}", dailyOhlcvs);
    }

    @Tag("manual")
    @Test
    void getBalance() throws Exception {
        // given
        // when
        Balance balance = getUpbitTradeClient().getBalance();
        // then
        log.info("balance: {}", balance);
    }

    @Tag("manual")
    @Test
    void submitOrderBuy() throws Exception {
        // given
        Asset asset = Asset.builder()
                .assetId("UPBIT.KRW-BTC")
                .build();
        Order order = Order.builder()
                .type(Order.Type.BUY)
                .assetId(asset.getAssetId())
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(6))
                .price(BigDecimal.valueOf(840))
                .build();
        // when
        getUpbitTradeClient().submitOrder(asset, order);
        // then
    }

    @Tag("manual")
    @Test
    void submitOrderSell() throws Exception {
        // given
        Asset asset = Asset.builder()
                .assetId("UPBIT.KRW-BTC")
                .build();
        Order order = Order.builder()
                .type(Order.Type.SELL)
                .assetId(asset.getAssetId())
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(0.00008556))
                .price(null)
                .build();
        // when
        getUpbitTradeClient().submitOrder(asset, order);
        // then
    }

    @Tag("manual")
    @Test
    void getWaitingOrders() throws Exception {
        // given
        // when
        List<Order> orders = getUpbitTradeClient().getWaitingOrders();
        // then
        log.info("orders:{}", orders);
    }

    @Tag("manual")
    @Test
    void amendOrder() throws Exception {
        // given
        List<Order> orders = getUpbitTradeClient().getWaitingOrders();
        // when
        for(Order order : orders) {
            Asset asset = Asset.builder()
                    .assetId(order.getAssetId())
                    .build();
            Order amendedOrder = getUpbitTradeClient().amendOrder(asset, order);
            log.debug("amendedOrder:{}", amendedOrder);
        }
    }

}