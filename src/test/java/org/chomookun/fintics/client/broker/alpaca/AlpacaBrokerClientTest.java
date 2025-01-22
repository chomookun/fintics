package org.chomookun.fintics.client.broker.alpaca;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.model.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class AlpacaBrokerClientTest extends CoreTestSupport {

    private static boolean live;

    private static String apiKey;

    private static String apiSecret;

    @BeforeAll
    static void beforeAll() {
        live = Optional.ofNullable(System.getenv("ALPACA_LIVE"))
                .map(Boolean::parseBoolean)
                .orElse(false);
        apiKey = System.getenv("ALPACA_API_KEY");
        apiSecret = System.getenv("ALPACA_API_SECRET");
    }

    AlpacaBrokerClient getAlpacaBrokerClient() {
        Properties properties = new Properties();
        properties.put("live", Boolean.toString(live));
        properties.put("apiKey", apiKey);
        properties.put("apiSecret", apiSecret);
        return new AlpacaBrokerClient(new AlpacaBrokerClientDefinition(), properties);
    }

    @Disabled
    @Test
    void isOpened() throws InterruptedException {
        // given
        LocalDateTime datetime = LocalDateTime.now();
        // when
        boolean opened = getAlpacaBrokerClient().isOpened(datetime);
        // then
        log.info("== opened:{}", opened);
    }

    @Disabled
    @Test
    void getOrderBook() throws InterruptedException {
        // given
        Asset tradeAsset = Asset.builder()
                .assetId("US.AAPL")
                .build();
        // when
        OrderBook orderBook = getAlpacaBrokerClient().getOrderBook(tradeAsset);
        log.info("== orderBook:{}", orderBook);
        // then
        assertNotNull(orderBook);
    }

    @Disabled
    @Test
    void getMinuteOhlcvs() throws InterruptedException {
        // given
        Asset tradeAsset = Asset.builder()
                .assetId("US.AAPL")
                .build();
        // when
        List<Ohlcv> minuteOhlcvs = getAlpacaBrokerClient().getMinuteOhlcvs(tradeAsset);
        // then
        assertNotNull(minuteOhlcvs);
    }

    @Disabled
    @Test
    void getDailyOhlcvs() throws InterruptedException {
        // given
        Asset tradeAsset = Asset.builder()
                .assetId("US.AAPL")
                .build();
        // when
        List<Ohlcv> dailyOhlcvs = getAlpacaBrokerClient().getDailyOhlcvs(tradeAsset);
        // then
        assertNotNull(dailyOhlcvs);
    }

    @Disabled
    @Test
    void getBalance() throws InterruptedException {
        // given
        // when
        Balance balance = getAlpacaBrokerClient().getBalance();

        // then
        assertNotNull(balance.getCashAmount());
    }

    @Disabled
    @Test
    void submitOrderBuyStock() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("US.AAPL")
                .build();
        Order order = Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getAlpacaBrokerClient().submitOrder(asset, order);
    }

    @Disabled
    @Test
    void submitOrderBuyEtf() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("US.AAPL")
                .build();
        Order order = Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getAlpacaBrokerClient().submitOrder(asset, order);
    }

    @Disabled
    @Test
    void submitOrderSellStock() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("US.AAPL")
                .build();
        Order order = Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.SELL)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();
        // when
        getAlpacaBrokerClient().submitOrder(asset, order);
    }

    @Disabled
    @Test
    void submitOrderSellEtf() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("US.AAPL")
                .build();
        Order order = Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.SELL)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();
        // when
        getAlpacaBrokerClient().submitOrder(asset, order);
    }

}