package org.chomookun.fintics.client.asset.market;

import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes= FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class UsAssetClientTest extends CoreTestSupport {

    private final AssetClientProperties assetClientProperties;

    private final ObjectMapper objectMapper;

    public UsAssetClient getUsAssetClient() {
        return new UsAssetClient(assetClientProperties, objectMapper);
    }

    /**
     * Returns test stock assets
     * @return test stock assets
     */
    static List<Asset> getTestStockAssets() {
        return List.of(
                Asset.builder()
                        .assetId("US.MSFT")
                        .name("Microsoft Corporation Common Stock")
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
                        .market("US")
                        .type("ETF")
                        .build()
        );
    }

    @Test
    void getAssets() {
        // when
        List<Asset> assets = getUsAssetClient().getAssets();
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
    void getStockAssets() {
        // given
        List<String> exchanges = List.of("NASDAQ", "NYSE", "AMEX");
        // when
        List<Asset> assets = new ArrayList<>();
        exchanges.forEach(exchange -> {
            List<Asset> exchangeAssets = getUsAssetClient().getStockAssets(exchange);
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
        List<Asset> assets = getUsAssetClient().getEtfAssets();
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
        List<Ohlcv> ohlcvs = getUsAssetClient().getOhlcvs(asset);
        // then
        assertFalse(ohlcvs.isEmpty());
    }

    @ParameterizedTest
    @MethodSource({"getTestStockAssets", "getTestEtfAssets"})
    void getDividends(Asset asset) {
        // when
        List<Dividend> dividends = getUsAssetClient().getDividends(asset);
        // then
        log.info("dividends:{}", dividends);
    }

    @ParameterizedTest
    @MethodSource("getTestStockAssets")
    void updateStockAsset(Asset asset) {
        // when
        getUsAssetClient().updateStockAsset(asset);
        // then
        assertNotNull(asset.getEps());
        assertNotNull(asset.getRoe());
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

    @ParameterizedTest
    @MethodSource("getTestEtfAssets")
    void updateEtfAsset(Asset asset) {
        // when
        getUsAssetClient().updateEtfAsset(asset);
        // then
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

}