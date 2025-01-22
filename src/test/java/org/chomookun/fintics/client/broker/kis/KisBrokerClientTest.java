package org.chomookun.fintics.client.broker.kis;

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
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KisBrokerClientTest extends CoreTestSupport {

    private static String production;

    private static String apiUrl;

    private static String appKey;

    private static String appSecret;

    private static String accountNo;

    @BeforeAll
    static void beforeAll() {
        production = System.getenv("KIS_PRODUCTION");
        apiUrl = System.getenv("KIS_API_URL");
        appKey = System.getenv("KIS_APP_KEY");
        appSecret = System.getenv("KIS_APP_SECRET");
        accountNo = System.getenv("KIS_ACCOUNT_NO");
    }

    KisBrokerClient getKisClient() {
        Properties properties = new Properties();
        properties.setProperty("production", production);
        properties.setProperty("apiUrl", apiUrl);
        properties.setProperty("appKey", appKey);
        properties.setProperty("appSecret", appSecret);
        properties.setProperty("accountNo", accountNo);
        return new KisBrokerClient(new KisBrokerClientDefinition(), properties);
    }

    @Disabled
    @Test
    void isOpened() throws InterruptedException {
        // given
        LocalDateTime datetime = LocalDateTime.now();
        // when
        boolean opened = getKisClient().isOpened(datetime);
        // then
        log.info("== opened:{}", opened);
    }

    @Disabled
    @Test
    void isHoliday() throws InterruptedException {
        // given
        LocalDateTime datetime = LocalDateTime.of(2020, 12, 25, 0, 0, 0);
        // when
        boolean holiday = getKisClient().isHoliday(datetime);
        // then
        log.info("== holiday: {}", holiday);
    }

    @Disabled
    @Test
    void getOrderBook() throws InterruptedException {
        // given
        Asset tradeAsset = Asset.builder()
                .assetId("KR.005930")
                .build();

        // when
        OrderBook orderBook = getKisClient().getOrderBook(tradeAsset);
        log.info("== orderBook:{}", orderBook);

        // then
        assertNotNull(orderBook);
    }

    @Disabled
    @Test
    void getMinuteOhlcvs() throws InterruptedException {
        // given
        Asset tradeAsset = Asset.builder()
                .assetId("KR.005930")
                .build();

        // when
        List<Ohlcv> minuteOhlcvs = getKisClient().getMinuteOhlcvs(tradeAsset);

        // then
        assertNotNull(minuteOhlcvs);
    }

    @Disabled
    @Test
    void getDailyOhlcvs() throws InterruptedException {
        // given
        Asset tradeAsset = Asset.builder()
                .assetId("KR.005930")
                .build();

        // when
        List<Ohlcv> dailyOhlcvs = getKisClient().getDailyOhlcvs(tradeAsset);

        // then
        assertNotNull(dailyOhlcvs);
    }

    @Disabled
    @Test
    void getBalance() throws InterruptedException {
        // given
        // when
        Balance balance = getKisClient().getBalance();

        // then
        assertNotNull(balance.getCashAmount());
    }

    @Disabled
    @Test
    void submitOrderBuyStock() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")   // Samsung Electronic
                .build();
        Order order = Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getKisClient().submitOrder(asset, order);
    }

    @Disabled
    @Test
    void submitOrderBuyEtf() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")   // Kodex 200 ETF
                .build();
        Order order = Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getKisClient().submitOrder(asset, order);
    }

    @Disabled
    @Test
    void submitOrderSellStock() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")   // Samsung Electronic
                .build();
        Order order = Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.SELL)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getKisClient().submitOrder(asset, order);
    }

    @Disabled
    @Test
    void submitOrderSellEtf() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")   // Kodex 200 ETF
                .build();
        Order order = Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.SELL)
                .kind(Order.Kind.MARKET)
                .quantity(BigDecimal.valueOf(1))
                .build();

        // when
        getKisClient().submitOrder(asset, order);
    }

}