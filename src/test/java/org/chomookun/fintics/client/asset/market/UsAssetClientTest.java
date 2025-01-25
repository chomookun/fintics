package org.chomookun.fintics.client.asset.market;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.client.asset.AssetClientProperties;
import org.chomookun.fintics.model.Asset;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

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

    @Disabled
    @Test
    void getStockAssets() {
        // given
        // when
        List<Asset> assets = getUsAssetClient().getAssets();
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
    void getEtfAssets() {
        // given
        // when
        List<Asset> assets = getUsAssetClient().getEtfAssets();
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
    void getStockAssetDetail() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.MSFT")
                .name("Microsoft Corporation Common Stock")
                .market("US")
                .type("STOCK")
                .build();
        // when
        Map<String,String> assetDetail = getUsAssetClient().getStockAssetDetail(asset);
        // then
        log.info("assetDetail: {}", assetDetail);
        assertNotNull(assetDetail.get("marketCap"));
        assertNotNull(assetDetail.get("eps"));
        assertNotNull(assetDetail.get("roe"));
        assertNotNull(assetDetail.get("per"));
        assertNotNull(assetDetail.get("dividendYield"));
    }

    @Disabled
    @Test
    void getEtfAssetDetail() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .market("US")
                .type("ETF")
                .build();
        // when
        Map<String,String> assetDetail = getUsAssetClient().getEtfAssetDetail(asset);
        // then
        log.info("assetDetail: {}", assetDetail);
        assertNotNull(assetDetail.get("marketCap"));
        assertNotNull(assetDetail.get("dividendYield"));
    }

    @Disabled
    @Test
    void getStockOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.MSFT")
                .name("Microsoft Corporation Common Stock")
                .market("US")
                .type("STOCK")
                .build();
        // when
        List<Map<String,String>> stockOhlcvs = getUsAssetClient().getOhlcvs(asset);
        // then
        log.info("stockOhlcvs:{}", stockOhlcvs);
    }

    @Disabled
    @Test
    void getEtfOhlcvs() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .market("US")
                .type("ETF")
                .build();
        // when
        List<Map<String,String>> etfOhlcvs = getUsAssetClient().getOhlcvs(asset);
        // then
        log.info("etfOhlcvs:{}", etfOhlcvs);
    }


}