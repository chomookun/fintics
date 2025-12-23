package org.chomookun.fintics.core.broker.client.kis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.balance.model.Balance;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.balance.model.RealizedProfit;
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
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KisUsBrokerClientTest extends CoreTestSupport {

    private static String production;

    private static String apiUrl;

    private static String appKey;

    private static String appSecret;

    private static String accountNo;

    @BeforeAll
    static void beforeAll() throws Exception {
        production = System.getenv("KIS_US_PRODUCTION");
        apiUrl = System.getenv("KIS_US_API_URL");
        appKey = System.getenv("KIS_US_APP_KEY");
        appSecret = System.getenv("KIS_US_APP_SECRET");
        accountNo = System.getenv("KIS_US_ACCOUNT_NO");
        // loads access token
        if (apiUrl != null && appKey != null && appSecret != null && accountNo != null) {
            KisBrokerClientTestUtil.loadAccessToken(apiUrl, appKey, appSecret);
        }
    }

    static List<Asset> getStockAssets() {
        return List.of(
                Asset.builder()
                        .assetId("US.MSFT")
                        .name("Microsoft Corporation Common Stock")
                        .market("US")
                        .exchange("XNAS")
                        .type("STOCK")
                        .build()
        );
    }

    static List<Asset> getEtfAssets() {
        return List.of(
                Asset.builder()
                        .assetId("US.SPY")
                        .market("US")
                        .exchange("XASE")
                        .type("ETF")
                        .build()
        );
    }

    KisUsBrokerClient getKisUsClient() {
        Properties properties = new Properties();
        properties.setProperty("production", production);
        properties.setProperty("apiUrl", apiUrl);
        properties.setProperty("appKey", appKey);
        properties.setProperty("appSecret", appSecret);
        properties.setProperty("accountNo", accountNo);
        return new KisUsBrokerClient(new KisBrokerClientDefinition(), properties);
    }

    @Tag("manual")
    @Test
    void isOpened() throws InterruptedException {
        // given
        LocalDateTime datetime = LocalDateTime.now();
        // when
        boolean opened = getKisUsClient().isOpened(datetime);
        // then
        log.info("== opened:{}", opened);
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource({"getStockAssets","getEtfAssets"})
    void getMinuteOhlcvs(Asset asset) throws InterruptedException {
        // when
        List<Ohlcv> minuteOhlcvs = getKisUsClient().getMinuteOhlcvs(asset);
        // then
        assertNotNull(minuteOhlcvs);
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource({"getStockAssets","getEtfAssets"})
    void getDailyOhlcvs(Asset asset) throws InterruptedException {
        // when
        List<Ohlcv> dailyOhlcvs = getKisUsClient().getDailyOhlcvs(asset);
        // then
        assertNotNull(dailyOhlcvs);
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource({"getStockAssets","getEtfAssets"})
    void getOrderBook(Asset asset) throws InterruptedException {
        // when
        OrderBook orderBook = getKisUsClient().getOrderBook(asset);
        log.info("== orderBook:{}", orderBook);
        // then
        assertNotNull(orderBook);
        assertNotNull(orderBook.getPrice());
        assertNotNull(orderBook.getTickPrice());
        assertNotNull(orderBook.getAskPrice());
        assertNotNull(orderBook.getAskPrice());
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource({"getStockAssets","getEtfAssets"})
    void getTickPrice(Asset asset) throws InterruptedException {
        // when
        BigDecimal tickPrice = getKisUsClient().getTickPrice(asset, BigDecimal.ZERO);
        // then
        log.info("tickPrice: {}", tickPrice);
        assertNotNull(tickPrice);
    }

    @Tag("manual")
    @Test
    void getBalance() throws InterruptedException {
        // when
        Balance balance = getKisUsClient().getBalance();
        // then
        assertNotNull(balance.getCashAmount());
    }

    @Tag("manual")
    @Test
    void submitOrderBuy() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("US.TSLA")
                .build();
        Order order = Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.BUY)
                .kind(Order.Kind.LIMIT)
                .quantity(BigDecimal.valueOf(1))
                .price(BigDecimal.valueOf(10))
                .build();
        // when
        getKisUsClient().submitOrder(asset, order);
    }

    @Tag("manual")
    @Test
    void submitOrderSell() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("US.TSLA")
                .build();
        Order order = Order.builder()
                .assetId(asset.getAssetId())
                .type(Order.Type.SELL)
                .kind(Order.Kind.LIMIT)
                .quantity(BigDecimal.valueOf(1))
                .price(BigDecimal.valueOf(1000))
                .build();
        // when
        getKisUsClient().submitOrder(asset, order);
    }

    @Tag("manual")
    @Test
    void getRealizedProfits() throws InterruptedException {
        // given
        LocalDate dateFrom = LocalDate.now().minusDays(30);
        LocalDate dateTo = LocalDate.now();
        // when
        List<RealizedProfit> realizedProfits = getKisUsClient().getRealizedProfits(dateFrom, dateTo);
        // then
        log.info("realizedProfit:{}",  realizedProfits);
    }

    @Tag("manual")
    @Test
    void getPaymentBalanceAsset() throws InterruptedException {
        // given
        LocalDate date = LocalDate.now().minusWeeks(4);
        String symbol = "MSFT";
        // when
        Map<String,String> paymentBalanceAsset = getKisUsClient().getPaymentBalanceAsset(date, symbol);
        // then
        log.info("paymentBalanceAsset:{}", paymentBalanceAsset);
    }

}