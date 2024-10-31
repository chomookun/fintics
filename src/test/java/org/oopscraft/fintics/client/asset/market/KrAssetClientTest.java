package org.oopscraft.fintics.client.asset.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.client.asset.AssetClientProperties;
import org.oopscraft.fintics.model.Asset;
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

    @Disabled
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

    @Disabled
    @Test
    void getStockAssetDetail() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .name("Samsung Electronics")
                .market("KR")
                .type("STOCK")
                .marketCap(BigDecimal.TEN)
                .build();
        // when
        Map<String,String> assetDetail = getKrAssetClient().getStockAssetDetail(asset);
        // then
        log.info("assetDetail: {}", assetDetail);
        assertNotNull(assetDetail.get("marketCap"));
        assertNotNull(assetDetail.get("eps"));
        assertNotNull(assetDetail.get("roe"));
        assertNotNull(assetDetail.get("roa"));
        assertNotNull(assetDetail.get("per"));
        assertNotNull(assetDetail.get("dividendYield"));
    }

    @Disabled
    @Test
    void getEtfAssetDetail() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.069500")
                .name("KODEX 200")
                .market("KR")
                .type("ETF")
                .marketCap(BigDecimal.TEN)
                .build();
        // when
        Map<String,String> assetDetail = getKrAssetClient().getEtfAssetDetail(asset);
        // then
        log.info("assetDetail: {}", assetDetail);
        assertNotNull(assetDetail.get("marketCap"));
        assertNotNull(assetDetail.get("dividendYield"));
    }

    @Disabled
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
        List<Map<String,String>> dividends = getKrAssetClient().getStockDividends(asset);
        // then
        log.info("dividends:{}", dividends);
    }

    @Disabled
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
        List<Map<String,String>> dividends = getKrAssetClient().getEtfDividends(asset);
        // then
        log.info("dividends:{}", dividends);
    }

}