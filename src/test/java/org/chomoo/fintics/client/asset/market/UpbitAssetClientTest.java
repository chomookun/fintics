package org.chomoo.fintics.client.asset.market;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomoo.fintics.client.asset.market.UpbitAssetClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.chomoo.arch4j.core.common.test.CoreTestSupport;
import org.chomoo.fintics.FinticsConfiguration;
import org.chomoo.fintics.client.asset.AssetClientProperties;
import org.chomoo.fintics.model.Asset;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsConfiguration.class)
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
        // given
        // when
        List<Asset> assets = getUpbitAssetClient().getAssets();
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

}