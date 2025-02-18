package org.chomookun.fintics.core.asset.client.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.asset.client.AssetClientProperties;
import org.chomookun.fintics.core.asset.model.Asset;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class UpbitAssetClientTest extends CoreTestSupport {

    private final AssetClientProperties assetClientProperties;

    public UpbitAssetClient getUpbitAssetClient() {
        return new UpbitAssetClient(assetClientProperties);
    }

    @Disabled
    @Test
    void getAssets() {
        // when
        List<Asset> assets = getUpbitAssetClient().getAssets();
        // then
        log.info("assets: {}", assets);
        assertFalse(assets.isEmpty());
        assertTrue(assets.stream().allMatch(asset ->
                asset.getAssetId() != null &&
                        asset.getName() != null &&
                        asset.getMarket() != null &&
                        asset.getExchange() != null &&
                        asset.getType() != null));
    }

}