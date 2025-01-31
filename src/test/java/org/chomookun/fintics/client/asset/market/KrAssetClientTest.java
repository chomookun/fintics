package org.chomookun.fintics.client.asset.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.client.asset.AssetClientProperties;
import org.chomookun.fintics.model.Asset;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes= FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class KrAssetClientTest extends CoreTestSupport {

    private final AssetClientProperties assetClientProperties;

    public KrAssetClient getKrAssetClient() {
        return new KrAssetClient(assetClientProperties);
    }

    @Test
    void getAssets() {
        // given
        // when
        List<Asset> assets = getKrAssetClient().getAssets();
        // then
        log.info("assets: {}", assets);
        assertTrue(assets.size() > 0);
        assertTrue(assets.stream().allMatch(asset ->
                asset.getAssetId() != null &&
                        asset.getName() != null &&
                        asset.getMarket() != null &&
                        asset.getExchange() != null &&
                        asset.getType() != null));
    }

    @Test
    void getSecInfo() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .name("Samsung Electronics")
                .market("KR")
                .type("STOCK")
                .marketCap(BigDecimal.TEN)
                .build();
        // when
        Map<String,String> secInfo = getKrAssetClient().getSecInfo(asset);
        // then
        log.info("secInfo:{}", secInfo);
    }

    @Test
    void getStockPrices() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .name("Samsung Electronics")
                .market("KR")
                .type("STOCK")
                .marketCap(BigDecimal.TEN)
                .build();
        // when
        Map<LocalDate, BigDecimal> prices = getKrAssetClient().getStockPrices(asset);
        // then
        log.info("prices:{}", prices);
        assertTrue(prices.size() > 0);
    }

    @Test
    void getStockDividends() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .name("Samsung Electronics")
                .market("KR")
                .type("STOCK")
                .marketCap(BigDecimal.TEN)
                .build();
        // when
        Map<LocalDate, BigDecimal> dividends = getKrAssetClient().getStockDividends(asset);
        // then
        log.info("dividends:{}", dividends);
    }

    @Test
    void getEtfPrices() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")
                .name("KODEX 200")
                .market("KR")
                .type("ETF")
                .build();
        // when
        Map<LocalDate, BigDecimal> prices = getKrAssetClient().getEtfPrices(asset);
        // then
        log.info("prices:{}", prices);
        assertTrue(prices.size() > 0);
    }

    @Test
    void getEtfDividends() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")
                .name("KODEX 200")
                .market("KR")
                .type("ETF")
                .build();
        // when
        Map<LocalDate, BigDecimal> dividends = getKrAssetClient().getEtfDividends(asset);
        // then
        log.info("dividends:{}", dividends);
    }

    @Test
    void updateStockAsset() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .name("Samsung Electronics")
                .market("KR")
                .type("STOCK")
                .marketCap(BigDecimal.TEN)
                .build();
        // when
        getKrAssetClient().updateStockAsset(asset);
        // then
        log.info("asset: {}", asset);
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