package org.chomookun.fintics.core.dividend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.dividend.model.Dividend;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class DefaultDividendClientTest extends CoreTestSupport {

    private final DividendClientProperties dividendClientProperties;

    private final ObjectMapper objectMapper;

    /**
     * Creates default dividend client
     * @return default dividend client
     */
    DefaultDividendClient createDefaultDividendClient() {
        return new DefaultDividendClient(dividendClientProperties, objectMapper);
    }

    static List<Asset> getTestAssets() {
        return List.of(
                Asset.builder()
                        .assetId("US.MSFT")
                        .name("Microsoft Corporation Common Stock")
                        .market("US")
                        .type("STOCK")
                        .build(),
                Asset.builder()
                        .assetId("US.SPY")
                        .market("US")
                        .type("ETF")
                        .build(),
                Asset.builder()
                        .assetId("KR.005930")
                        .name("Samsung Electronics")
                        .market("KR")
                        .type("STOCK")
                        .marketCap(BigDecimal.TEN)
                        .build(),
                Asset.builder()
                        .assetId("KR.069500")
                        .name("KODEX 200")
                        .market("KR")
                        .type("ETF")
                        .build()
        );
    }

    @ParameterizedTest
    @MethodSource("getTestAssets")
    void getDividends(Asset asset) {
        // given
        LocalDate dateFrom = LocalDate.now().minusYears(3);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = createDefaultDividendClient().getDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

}