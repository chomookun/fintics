package org.chomookun.fintics.client.asset;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.client.asset.market.KrAssetClient;
import org.chomookun.fintics.client.asset.market.UpbitAssetClient;
import org.chomookun.fintics.client.asset.market.UsAssetClient;
import org.chomookun.fintics.model.Asset;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "fintics", name = "asset-client.class-name", havingValue="org.chomookun.fintics.client.asset.SimpleAssetClient")
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
    public boolean isSupport(Asset asset) {
        for(AssetClient assetClient : assetClients) {
            if (assetClient.isSupport(asset)) {
                return true;
            }
        }
        return false;
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
    public void populateAsset(Asset asset) {
        for (AssetClient assetClient : assetClients) {
            if (assetClient.isSupport(asset)) {
                assetClient.populateAsset(asset);
                return;
            }
        }
    }

}
