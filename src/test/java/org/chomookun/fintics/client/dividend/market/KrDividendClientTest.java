package org.chomookun.fintics.client.dividend.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.client.dividend.DividendClientProperties;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.Dividend;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(classes= FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KrDividendClientTest extends CoreTestSupport {

    private final DividendClientProperties dividendClientProperties;

    public KrDividendClient getKrDividendClient() {
        return new KrDividendClient(dividendClientProperties);
    }

    static List<Asset> getStockAssets() {
       return List.of(
               Asset.builder()
                       .assetId("KR.005930")
                       .name("Samsung Electronics")
                       .market("KR")
                       .type("STOCK")
                       .marketCap(BigDecimal.TEN)
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

    @ParameterizedTest
    @MethodSource("getStockAssets")
    void getStockDividends(Asset asset) {
        // given
        LocalDate dateFrom = LocalDate.now().minusYears(3);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = getKrDividendClient().getStockDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

    @ParameterizedTest
    @MethodSource("getEtfAssets")
    void getEtfDividends(Asset asset) {
        // given
        LocalDate dateFrom = LocalDate.now().minusYears(3);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = getKrDividendClient().getEtfDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

    @ParameterizedTest
    @MethodSource({"getStockAssets", "getEtfAssets"})
    void getDividends(Asset asset) {
        // given
        LocalDate dateFrom = LocalDate.now().minusYears(3);
        LocalDate dateTo = LocalDate.now();
        // when
        List<Dividend> dividends = getKrDividendClient().getDividends(asset, dateFrom, dateTo);
        // then
        log.info("dividends:{}", dividends);
    }

}