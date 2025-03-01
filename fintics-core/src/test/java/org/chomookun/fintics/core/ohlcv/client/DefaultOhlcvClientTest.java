package org.chomookun.fintics.core.ohlcv.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class DefaultOhlcvClientTest extends CoreTestSupport {

    private final OhlcvClientProperties ohlcvClientProperties;

    private final ObjectMapper objectMapper;

    DefaultOhlcvClient createDefaultOhlcvClient() {
        return new DefaultOhlcvClient(ohlcvClientProperties, objectMapper);
    }

    static List<Asset> getTestUsStockAssets() {
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

    static List<Asset> getTestUsEtfAssets() {
        return List.of(
                Asset.builder()
                        .assetId("US.SPY")
                        .market("US")
                        .exchange("XASE")
                        .type("ETF")
                        .build()
        );
    }

    static List<Asset> getTestKrStockAssets() {
        return List.of(
                Asset.builder()
                        .assetId("KR.005930")
                        .name("Samsung Electronics")
                        .market("KR")
                        .exchange("XKRX")
                        .type("STOCK")
                        .marketCap(BigDecimal.TEN)
                        .build()
        );
    }

    static List<Asset> getTestKrEtfAssets() {
        return List.of(
                Asset.builder()
                        .assetId("KR.069500")
                        .name("KODEX 200")
                        .market("KR")
                        .exchange("XKRX")
                        .type("ETF")
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource({"getTestUsStockAssets","getTestUsEtfAssets","getTestKrStockAssets","getTestKrEtfAssets"})
    void getDailyOhlcvs(Asset asset) {
        // given
        Ohlcv.Type type = Ohlcv.Type.DAILY;
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusYears(1);
        LocalDateTime dateTimeTo = LocalDateTime.now();
        Pageable pageable = Pageable.unpaged();
        // when
        List<Ohlcv> dailyOhlcvs = createDefaultOhlcvClient().getOhlcvs(asset, type, dateTimeFrom, dateTimeTo);
        // then
        log.info("dailyOhlcvs:{}", dailyOhlcvs);
        assertFalse(dailyOhlcvs.isEmpty());
    }

    @ParameterizedTest
    @MethodSource({"getTestUsStockAssets","getTestUsEtfAssets","getTestKrStockAssets","getTestKrEtfAssets"})
    void getMinuteOhlcvs(Asset asset) {
        // given
        Ohlcv.Type type = Ohlcv.Type.MINUTE;
        LocalDateTime dateTimeFrom = LocalDateTime.now().minusDays(30);
        LocalDateTime dateTimeTo = LocalDateTime.now();
        Pageable pageable = Pageable.unpaged();
        // when
        List<Ohlcv> minuteOhlcvs = createDefaultOhlcvClient().getOhlcvs(asset, type, dateTimeFrom, dateTimeTo);
        // then
        log.info("minuteOhlcvs:{}", minuteOhlcvs);
        assertFalse(minuteOhlcvs.isEmpty());
    }

}