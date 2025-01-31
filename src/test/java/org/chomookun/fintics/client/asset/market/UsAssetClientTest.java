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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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

    @Test
    void getAssets() {
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
    void getStockPrices() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.MSFT")
                .name("Microsoft Corporation Common Stock")
                .market("US")
                .type("STOCK")
                .build();
        // when
        Map<LocalDate, BigDecimal> stockPrices = getUsAssetClient().getPrices(asset);
        // then
        log.info("stockPrice:{}", stockPrices);
        assertTrue(stockPrices.size() > 0);
    }

    @Test
    void getEtfPrices() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .market("US")
                .type("ETF")
                .build();
        // when
        Map<LocalDate, BigDecimal> etfPrices = getUsAssetClient().getPrices(asset);
        // then
        log.info("etfPrices:{}", etfPrices);
        assertTrue(etfPrices.size() > 0);
    }

    @Test
    void getStockDividends() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.MSFT")
                .name("Microsoft Corporation Common Stock")
                .market("US")
                .type("STOCK")
                .build();
        // when
        Map<LocalDate, BigDecimal> stockDividends = getUsAssetClient().getDividends(asset);
        // then
        log.info("stockDividends:{}", stockDividends);
    }

    @Test
    void getEtfDividends() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .market("US")
                .type("ETF")
                .build();
        // when
        Map<LocalDate, BigDecimal> etfDividends = getUsAssetClient().getDividends(asset);
        // then
        log.info("etfDividends:{}", etfDividends);
        assertTrue(etfDividends.size() > 0);
    }

    @Test
    void updateStockAsset() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.MSFT")
                .name("Microsoft Corporation Common Stock")
                .market("US")
                .type("STOCK")
                .build();
        // when
        getUsAssetClient().updateStockAsset(asset);
        // then
        assertNotNull(asset.getEps());
        assertNotNull(asset.getRoe());
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

    @Test
    void updateEtfAsset() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.SPY")
                .market("US")
                .type("ETF")
                .build();
        // when
        getUsAssetClient().updateEtfAsset(asset);
        // then
        assertNotNull(asset.getCapitalGain());
        assertNotNull(asset.getTotalReturn());
    }

}