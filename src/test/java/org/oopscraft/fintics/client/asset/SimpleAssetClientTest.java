package org.oopscraft.fintics.client.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.oopscraft.arch4j.core.common.test.CoreTestSupport;
import org.oopscraft.fintics.FinticsConfiguration;
import org.oopscraft.fintics.model.Asset;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

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

    @Test
    void getAssets() {
        // given
        // when
        List<Asset> assets = getSimpleAssetClient().getAssets();
        // then
        log.info("assets: {}", assets);
        assertTrue(assets.size() > 0);
    }

    @Test
    void getAssetDetailForUs() {
        // given
        Asset asset = Asset.builder()
                .assetId("US.MSFT")
                .name("Microsoft Corporation Common Stock")
                .market("US")
                .type("STOCK")
                .build();
        // when
        Map<String,String> assetDetail = getSimpleAssetClient().getAssetDetail(asset);
        // then
        log.info("assetDetail: {}", assetDetail);
    }

    @Test
    void getAssetDetailForKr() {
        // given
        Asset asset = Asset.builder()
                .assetId("KR.005930")
                .name("Samsung Electronics")
                .market("KR")
                .type("STOCK")
                .build();
        // when
        Map<String,String> assetDetail = getSimpleAssetClient().getAssetDetail(asset);
        // then
        log.info("assetDetail: {}", assetDetail);
    }

}