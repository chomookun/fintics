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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KisUsBrokerClientTest extends CoreTestSupport {

    private static String production;

    private static String apiUrl;

    private static String appKey;

    private static String appSecret;

    private static String accountNo;

    @BeforeAll
    static void beforeAll() {
        production = System.getenv("KIS_US_PRODUCTION");
        apiUrl = System.getenv("KIS_US_API_URL");
        appKey = System.getenv("KIS_US_APP_KEY");
        appSecret = System.getenv("KIS_US_APP_SECRET");
        accountNo = System.getenv("KIS_US_ACCOUNT_NO");
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

    @Disabled
    @Test
    void isOpened() throws InterruptedException {
        // given
        LocalDateTime datetime = LocalDateTime.now();
        // when
        boolean opened = getKisUsClient().isOpened(datetime);
        // then
        log.info("== opened:{}", opened);
    }

    @Disabled
    @Test
    void getOrderBook() throws InterruptedException {
        // given
        Asset tradeAsset = Asset.builder()
                .assetId("US.TSLA")
                .exchange("XNAS")
                .build();

        // when
        OrderBook orderBook = getKisUsClient().getOrderBook(tradeAsset);
        log.info("== orderBook:{}", orderBook);

        // then
        assertNotNull(orderBook);
    }


    @Disabled
    @Test
    void getMinuteOhlcvs() throws InterruptedException {
        // given
        Asset tradeAsset = Asset.builder()
                .assetId("US.TSLA")
                .exchange("XNAS")
                .build();

        // when
        List<Ohlcv> minuteOhlcvs = getKisUsClient().getMinuteOhlcvs(tradeAsset);

        // then
        assertNotNull(minuteOhlcvs);
    }

    @Disabled
    @Test
    void getDailyOhlcvs() throws InterruptedException {
        // given
        Asset tradeAsset = Asset.builder()
                .assetId("US.TSLA")
                .exchange("XNAS")
                .build();

        // when
        List<Ohlcv> dailyOhlcvs = getKisUsClient().getDailyOhlcvs(tradeAsset);

        // then
        assertNotNull(dailyOhlcvs);
    }

    @Disabled
    @Test
    void getBalance() throws InterruptedException {
        // given
        // when
        Balance balance = getKisUsClient().getBalance();

        // then
        assertNotNull(balance.getCashAmount());
    }

    @Disabled
    @Test
    void getTickPrice() throws InterruptedException {
        // given
        Asset asset = Asset.builder()
                .assetId("US.TSLA")
                .build();
        // when
        BigDecimal tickPrice = getKisUsClient().getTickPrice(asset, BigDecimal.ZERO);

        // then
        log.info("tickPrice: {}", tickPrice);
    }

    @Disabled
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

    @Disabled
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

    @Disabled
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

}