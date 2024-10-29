package org.oopscraft.fintics.client.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.asset.market.KrAssetClient;
import org.oopscraft.fintics.client.asset.market.UpbitAssetClient;
import org.oopscraft.fintics.client.asset.market.UsAssetClient;
import org.oopscraft.fintics.model.Asset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "asset-client.class-name", havingValue="org.oopscraft.fintics.client.asset.SimpleAssetClient")
@Slf4j
public class SimpleAssetClient extends AssetClient {

    private final List<AssetClient> assetClients = new ArrayList<>();

    protected SimpleAssetClient(AssetClientProperties assetClientProperties, ObjectMapper objectMapper) {
        super(assetClientProperties);
        assetClients.add(new UsAssetClient(assetClientProperties, objectMapper));
        assetClients.add(new KrAssetClient(assetClientProperties));
        assetClients.add(new UpbitAssetClient(assetClientProperties));
    }

    @Override
    public List<Asset> getAssets() {
        List<Asset> assets = new ArrayList<>();
        for(AssetClient assetClient : assetClients) {
            try {
                assets.addAll(assetClient.getAssets());
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
        return assets;
    }

    @Override
    public boolean isSupportAssetDetail(Asset asset) {
        for(AssetClient assetClient : assetClients) {
            if (assetClient.isSupportAssetDetail(asset)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Map<String,String> getAssetDetail(Asset asset) {
        for (AssetClient assetClient : assetClients) {
            if (assetClient.isSupportAssetDetail(asset)) {
                return assetClient.getAssetDetail(asset);
            }
        }
        return Collections.emptyMap();
    }

}
