package org.chomookun.fintics.core.broker.client.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.balance.model.Balance;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.balance.model.DividendProfit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
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
        // loads access token
        if (apiUrl != null && appKey != null && appSecret != null && accountNo != null) {
            KisBrokerClientTestUtil.loadAccessToken(apiUrl, appKey, appSecret);
        }
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

    static List<Asset> getStockAssets() {
        return List.of(
                Asset.builder()
                        .assetId("KR.005930")
                        .name("Samsung Electronics")
                        .market("KR")
                        .type("STOCK")
                        .marketCap(BigDecimal.TEN)
                        .build(),
                Asset.builder()
                        .assetId("KR.030000")
                        .name("Cheil Worldwide")
                        .market("KR")
                        .type("STOCK")
                        .build()
        );
    }

    static List<Asset> getEtfAssets() {
        return List.of(
                Asset.builder()
                        .assetId("KR.069500")
                        .name("KODEX 200")
                        .market("KR")
                        .type("ETF")
                        .build()
        );
    }

    @Tag("manual")
    @Test
    void isOpened() throws InterruptedException {
        // given
        LocalDateTime datetime = LocalDateTime.now();
        // when
        boolean opened = getKisClient().isOpened(datetime);
        // then
        log.info("== opened:{}", opened);
    }

    @Tag("manual")
    @Test
    void isHoliday() throws InterruptedException {
        // given
        LocalDateTime datetime = LocalDateTime.of(2020, 12, 25, 0, 0, 0);
        // when
        boolean holiday = getKisClient().isHoliday(datetime);
        // then
        log.info("== holiday: {}", holiday);
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource({"getStockAssets", "getEtfAssets"})
    void getMinuteOhlcvs(Asset asset) throws InterruptedException {
        // when
        List<Ohlcv> minuteOhlcvs = getKisClient().getMinuteOhlcvs(asset);
        // then
        assertNotNull(minuteOhlcvs);
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource({"getStockAssets", "getEtfAssets"})
    void getDailyOhlcvs(Asset asset) throws InterruptedException {
        // when
        List<Ohlcv> dailyOhlcvs = getKisClient().getDailyOhlcvs(asset);
        // then
        assertNotNull(dailyOhlcvs);
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource({"getStockAssets", "getEtfAssets"})
    void getOrderBook(Asset asset) throws InterruptedException {
        // when
        OrderBook orderBook = getKisClient().getOrderBook(asset);
        log.info("== orderBook:{}", orderBook);
        // then
        assertNotNull(orderBook);
        assertNotNull(orderBook.getPrice());
        assertNotNull(orderBook.getTickPrice());
        assertNotNull(orderBook.getAskPrice());
        assertNotNull(orderBook.getAskPrice());
    }

    @Tag("manual")
    @Test
    void getBalance() throws InterruptedException {
        // given
        // when
        Balance balance = getKisClient().getBalance();
        // then
        assertNotNull(balance.getCashAmount());
    }

    @Tag("manual")
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
        // then
        log.info("order: {}", order);
    }

    @Tag("manual")
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

    @Tag("manual")
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

    @Tag("manual")
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

    @Tag("manual")
    @Test
    void getWaitingOrders() throws InterruptedException {
        // when
        List<Order> waitingOrders = getKisClient().getWaitingOrders();
        // then
        log.info("waitingOrders:{}", waitingOrders);
    }

    @Tag("manual")
    @Test
    void getDividendProfits() throws InterruptedException {
        // given
        LocalDate dateFrom = LocalDate.now().minusYears(1);
        LocalDate dateTo = LocalDate.now();
        // when
        List<DividendProfit> dividendProfits = getKisClient().getDividendProfits(dateFrom, dateTo);
        // then
        assertNotNull(dividendProfits);
    }

}