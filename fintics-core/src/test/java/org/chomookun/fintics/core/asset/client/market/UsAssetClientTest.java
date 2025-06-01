package org.chomookun.fintics.core.asset.client.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.dividend.model.Dividend;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.asset.client.AssetClientProperties;
import org.chomookun.fintics.core.asset.model.Asset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes= FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class UsAssetClientTest extends CoreTestSupport {

    private final AssetClientProperties assetClientProperties;

    private final ObjectMapper objectMapper;

    /**
     * Creates us asset client
     * @return us asset client
     */
    UsAssetClient createUsAssetClient() {
        return new UsAssetClient(assetClientProperties, objectMapper);
    }

    /**
     * Returns test stock assets
     * @return test stock assets
     */
    static List<Asset> getTestStockAssets() {
        return List.of(
                // Large cap
                Asset.builder()
                        .assetId("US.MSFT")
                        .name("Microsoft Corporation Common Stock")
                        .market("US")
                        .type("STOCK")
                        .build(),
                Asset.builder()
                        .assetId("US.AAPL")
                        .name("Apple Inc. Common Stock")
                        .market("US")
                        .type("STOCK")
                        .build(),
                Asset.builder()
                        .assetId("US.GOOG")
                        .name("Alphabet Inc. Class C Capital Stock")
                        .market("US")
                        .type("STOCK")
                        .build(),
                // Mid cap
                Asset.builder()
                        .assetId("US.ADSK")
                        .name("Autodesk Inc. Common Stock")
                        .market("US")
                        .type("STOCK")
                        .build(),
                // ADR
                Asset.builder()
                        .assetId("US.TSM")
                        .name("Taiwan Semiconductor Manufacturing Company Limited American Depositary Shares")
                        .market("US")
                        .type("STOCK")
                        .build()
        );
    }

    /**
     * Returns test ETF assets
     * @return test ETF assets
     */
    static List<Asset> getTestEtfAssets() {
        return List.of(
                Asset.builder()
                        .assetId("US.SPY")
                        .name("SPDR S&P 500 ETF Trust")
                        .market("US")
                        .type("ETF")
                        .build(),
                Asset.builder()
                        .assetId("US.RDVI")
                        .name("FT Vest Rising Dividend Achievers Target Income ETF")
                        .market("US")
                        .type("ETF")
                        .build()
        );
    }

    @Test
    void getStockAssets() {
        // given
        List<String> exchanges = List.of("NASDAQ", "NYSE", "AMEX");
        // when
        List<Asset> assets = new ArrayList<>();
        exchanges.forEach(exchange -> {
            List<Asset> exchangeAssets = createUsAssetClient().getStockAssets(exchange);
            assets.addAll(exchangeAssets);
        });
        // then
        assertFalse(assets.isEmpty());
        assertTrue(assets.stream().allMatch(asset ->
                asset.getAssetId() != null &&
                        asset.getName() != null &&
                        asset.getMarket() != null &&
                        asset.getExchange() != null &&
                        asset.getType() != null));
    }

    @Test
    void getEtfAssets() {
        // when
        List<Asset> assets = createUsAssetClient().getEtfAssets();
        // then
        assertFalse(assets.isEmpty());
        assertTrue(assets.stream().allMatch(asset ->
                asset.getAssetId() != null &&
                        asset.getName() != null &&
                        asset.getMarket() != null &&
                        asset.getExchange() != null &&
                        asset.getType() != null));
    }

    @Test
    void getAssets() {
        // when
        List<Asset> assets = createUsAssetClient().getAssets();
        // then
        assertFalse(assets.isEmpty());
        assertTrue(assets.stream().allMatch(asset ->
                asset.getAssetId() != null &&
                        asset.getName() != null &&
                        asset.getMarket() != null &&
                        asset.getExchange() != null &&
                        asset.getType() != null));
    }

    @ParameterizedTest
    @MethodSource({"getTestStockAssets", "getTestEtfAssets"})
    void getOhlcvs(Asset asset) {
        // when
        List<Ohlcv> ohlcvs = createUsAssetClient().getOhlcvs(asset);
        // then
        assertFalse(ohlcvs.isEmpty());
    }

    @ParameterizedTest
    @MethodSource({"getTestStockAssets", "getTestEtfAssets"})
    void getDividends(Asset asset) {
        // when
        List<Dividend> dividends = createUsAssetClient().getDividends(asset);
        // then
        log.info("dividends:{}", dividends);
    }

    @ParameterizedTest
    @MethodSource("getTestStockAssets")
    void populateStockAsset(Asset asset) {
        // when
        createUsAssetClient().populateStockAsset(asset);
        // then
        assertNotNull(asset.getPrice());
        assertNotNull(asset.getVolume());
        assertNotNull(asset.getEps());
        assertNotNull(asset.getRoe());
        assertNotNull(asset.getPer());
        assertNotNull(asset.getDividendYield());
        assertNotNull(asset.getDividendFrequency());
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void populateEtfAsset(Asset asset) {
        // when
        createUsAssetClient().populateEtfAsset(asset);
        // then
        assertNotNull(asset.getPrice());
        assertNotNull(asset.getVolume());
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

}