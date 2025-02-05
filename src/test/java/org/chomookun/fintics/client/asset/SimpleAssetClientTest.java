package org.chomookun.fintics.client.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.model.Asset;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class SimpleAssetClientTest extends CoreTestSupport {

    private final AssetClientProperties assetClientProperties;

    private final ObjectMapper objectMapper;

    SimpleAssetClient getSimpleAssetClient() {
        return new SimpleAssetClient(assetClientProperties, objectMapper);
    }

    static List<Asset> getTestAssets() {
        return List.of(
                Asset.builder()
                        .assetId("US.MSFT")
                        .name("Microsoft Corporation Common Stock")
                        .market("US")
                        .type("STOCK")
                        .build(),
                Asset.builder()
                        .assetId("US.SPY")
                        .market("US")
                        .type("ETF")
                        .build(),
                Asset.builder()
                        .assetId("KR.005930")
                        .name("Samsung Electronics")
                        .market("KR")
                        .type("STOCK")
                        .marketCap(BigDecimal.TEN)
                        .build(),
                Asset.builder()
                        .assetId("KR.069500")
                        .name("KODEX 200")
                        .market("KR")
                        .type("ETF")
                        .build()
        );
    }

    @Disabled
    @Test
    void getAssets() {
        // given
        // when
        List<Asset> assets = getSimpleAssetClient().getAssets();
        // then
        log.info("assets: {}", assets);
        assertFalse(assets.isEmpty());
    }

    @Disabled
    @ParameterizedTest
    @MethodSource("getTestAssets")
    void updateAsset(Asset asset) {
        // when
        getSimpleAssetClient().updateAsset(asset);
        // then
        log.info("asset: {}", asset);
    }

}