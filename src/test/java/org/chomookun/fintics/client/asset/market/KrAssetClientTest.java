package org.chomookun.fintics.client.asset.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.model.Dividend;
import org.chomookun.fintics.model.Ohlcv;
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
                        .build()
        );
    }

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

    @ParameterizedTest
    @MethodSource("getTestStockAssets")
    void getStockOhlcvs(Asset asset) {
        // when
        List<Ohlcv> stockOhlcvs = getKrAssetClient().getStockOhlcvs(asset);
        // then
        assertFalse(stockOhlcvs.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void getStockDividends(Asset asset) {
        // when
        List<Dividend> stockDividends = getKrAssetClient().getStockDividends(asset);
        // then
        log.info("stockDividends:{}", stockDividends);
    }

    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void getEtfOhlcvs(Asset asset) {
        // when
        List<Ohlcv> etfOhlcvs = getKrAssetClient().getEtfOhlcvs(asset);
        // then
        assertFalse(etfOhlcvs.isEmpty());
    }

    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void getEtfDividends(Asset asset) {
        // when
        List<Dividend> etfDividends = getKrAssetClient().getEtfDividends(asset);
        // then
        log.info("etfDividends:{}", etfDividends);
    }

    @ParameterizedTest
    @MethodSource("getTestStockAssets")
    void updateStockAsset(Asset asset) {
       // when
        getKrAssetClient().updateStockAsset(asset);
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

    @Test
    void updateEtfAsset() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")
                .name("KODEX 200")
                .market("KR")
                .type("ETF")
                .marketCap(BigDecimal.TEN)
                .build();
        // when
        getKrAssetClient().updateEtfAsset(asset);
        // then
        log.info("asset: {}", asset);
        assertNotNull(asset.getDividendFrequency());
        assertNotNull(asset.getDividendYield());
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

}