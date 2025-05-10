package org.chomookun.fintics.core.dividend.client.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.dividend.client.DividendClientProperties;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.dividend.model.Dividend;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;

@SpringBootTest(classes= FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class UsDividendClientTest extends CoreTestSupport {

    final DividendClientProperties dividendClientProperties;

    final ObjectMapper objectMapper;

    /**
     * Creates US dividend client
     * @return us dividend client
     */
    public UsDividendClient createUsDividendClient() {
        return new UsDividendClient(dividendClientProperties, objectMapper);
    }

    static List<Asset> getStockAssets() {
       return List.of(
               Asset.builder()
                       .assetId("US.MSFT")
                       .name("Microsoft Corporation Common Stock")
                       .market("US")
                       .type("STOCK")
                       .build()
       );
    }

    static List<Asset> getEtfAssets() {
        return List.of(
                Asset.builder()
                        .assetId("US.SPY")
                        .market("US")
                        .type("ETF")
                        .build()
        );
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource("getStockAssets")
    void getStockDividends(Asset asset) {
        // given
        LocalDate dateFrom = LocalDate.now().minusYears(3);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = createUsDividendClient().getDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource("getEtfAssets")
    void getEtfDividends(Asset asset) {
        // given
        LocalDate dateFrom = LocalDate.now().minusYears(3);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = createUsDividendClient().getDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

    @Tag("manual")
    @ParameterizedTest
    @MethodSource({"getStockAssets", "getEtfAssets"})
    void getDividends(Asset asset) {
        // given
        LocalDate dateFrom = LocalDate.now().minusYears(3);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = createUsDividendClient().getDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

}