package org.chomookun.fintics.core.asset.client.market;

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

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes= FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KrAssetClientTest extends CoreTestSupport {

    private final AssetClientProperties assetClientProperties;

    /**
     * Creates kr asset client
     * @return KrAssetClient kr asset client
     */
    KrAssetClient createKrAssetClient() {
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

    @Test
    void getEtfAssets() {
        // when
        List<Asset> etfAssets = createKrAssetClient().getEtfAssets();
        // then
        assertFalse(etfAssets.isEmpty());
    }

    @Test
    void getAssets() {
        // when
        List<Asset> assets = createKrAssetClient().getAssets();
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
    @MethodSource("getTestStockAssets")
    void getStockOhlcvs(Asset asset) {
        // when
        List<Ohlcv> stockOhlcvs = createKrAssetClient().getStockOhlcvs(asset);
        // then
        assertFalse(stockOhlcvs.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void getStockDividends(Asset asset) {
        // when
        List<Dividend> stockDividends = createKrAssetClient().getStockDividends(asset);
        // then
        log.info("stockDividends:{}", stockDividends);
    }

    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void getEtfOhlcvs(Asset asset) {
        // when
        List<Ohlcv> etfOhlcvs = createKrAssetClient().getEtfOhlcvs(asset);
        // then
        assertFalse(etfOhlcvs.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void getEtfDividends(Asset asset) {
        // when
        List<Dividend> etfDividends = createKrAssetClient().getEtfDividends(asset);
        // then
        log.info("etfDividends:{}", etfDividends);
    }

    @ParameterizedTest
    @MethodSource("getTestStockAssets")
    void populateStockAsset(Asset asset) {
       // when
        createKrAssetClient().populateStockAsset(asset);
        // then
        log.info("asset:{}", asset);
        assertNotNull(asset.getRoe());
        assertNotNull(asset.getPer());
        assertNotNull(asset.getDividendFrequency());
        assertNotNull(asset.getDividendYield());
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void populateEtfAsset(Asset asset) {
        // when
        createKrAssetClient().populateEtfAsset(asset);
        // then
        log.info("asset: {}", asset);
        assertNotNull(asset.getDividendFrequency());
        assertNotNull(asset.getDividendYield());
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

}