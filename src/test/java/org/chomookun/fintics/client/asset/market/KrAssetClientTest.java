package org.chomookun.fintics.client.asset.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.model.Dividend;
import org.chomookun.fintics.model.Ohlcv;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.client.asset.AssetClientProperties;
import org.chomookun.fintics.model.Asset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes= FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KrAssetClientTest extends CoreTestSupport {

    private final AssetClientProperties assetClientProperties;

    /**
     * Returns KrAssetClient
     * @return KrAssetClient
     */
    public KrAssetClient getKrAssetClient() {
        return new KrAssetClient(assetClientProperties);
    }

    /**
     * Returns test stock assets
     * @return test stock assets
     */
    static List<Asset> getTestStockAssets() {
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

    /**
     * Returns test ETF assets
     * @return test ETF assets
     */
    static List<Asset> getTestEtfAssets() {
        return List.of(
                Asset.builder()
                        .assetId("KR.069500")
                        .name("KODEX 200")
                        .market("KR")
                        .type("ETF")
                        .build(),
                Asset.builder()
                        .assetId("KR.477080")
                        .name("RISE CD금리액티브(합성)")
                        .market("KR")
                        .type("ETF")
                        .build(),
                Asset.builder()
                        .assetId("KR.466920")
                        .name("SOL 조선TOP3플러스")
                        .market("KR")
                        .type("ETF")
                        .build(),
                Asset.builder()
                        .assetId("KR.487340")
                        .name("ACE CD금리&초단기채권액티비")
                        .market("KR")
                        .type("ETF")
                        .build()
                );
    }

    @Disabled
    @Test
    void getAssets() {
        // when
        List<Asset> assets = getKrAssetClient().getAssets();
        // then
        assertFalse(assets.isEmpty());
        assertTrue(assets.stream().allMatch(asset ->
                asset.getAssetId() != null &&
                        asset.getName() != null &&
                        asset.getMarket() != null &&
                        asset.getExchange() != null &&
                        asset.getType() != null));
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("getTestStockAssets")
    void getStockOhlcvs(Asset asset) {
        // when
        List<Ohlcv> stockOhlcvs = getKrAssetClient().getStockOhlcvs(asset);
        // then
        assertFalse(stockOhlcvs.isEmpty());
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void getStockDividends(Asset asset) {
        // when
        List<Dividend> stockDividends = getKrAssetClient().getStockDividends(asset);
        // then
        log.info("stockDividends:{}", stockDividends);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void getEtfOhlcvs(Asset asset) {
        // when
        List<Ohlcv> etfOhlcvs = getKrAssetClient().getEtfOhlcvs(asset);
        // then
        assertFalse(etfOhlcvs.isEmpty());
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void getEtfDividends(Asset asset) {
        // when
        List<Dividend> etfDividends = getKrAssetClient().getEtfDividends(asset);
        // then
        log.info("etfDividends:{}", etfDividends);
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("getTestStockAssets")
    void populateStockAsset(Asset asset) {
       // when
        getKrAssetClient().populateStockAsset(asset);
        // then
        log.info("asset:{}", asset);
        assertNotNull(asset.getEps());
        assertNotNull(asset.getRoe());
        assertNotNull(asset.getPer());
        assertNotNull(asset.getDividendFrequency());
        assertNotNull(asset.getDividendYield());
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void populateEtfAsset(Asset asset) {
        // when
        getKrAssetClient().populateEtfAsset(asset);
        // then
        log.info("asset: {}", asset);
        assertNotNull(asset.getDividendFrequency());
        assertNotNull(asset.getDividendYield());
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

}