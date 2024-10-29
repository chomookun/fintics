package org.oopscraft.fintics.client.asset;

import lombok.Getter;
import org.oopscraft.fintics.model.Asset;

import java.util.List;
import java.util.Map;

public abstract class AssetClient {

    @Getter
    private final AssetClientProperties assetClientProperties;

    /**
     * constructor
     * @param assetClientProperties asset client properties
     */
    protected AssetClient(AssetClientProperties assetClientProperties) {
        this.assetClientProperties = assetClientProperties;
    }

    /**
     * returns assets
     * @return assets
     */
    public abstract List<Asset> getAssets();

    /**
     * checks support asset detail
     * @param asset asset
     * @return support detail or not
     */
    public abstract boolean isSupportAssetDetail(Asset asset);

    /**
     * return asset details
     * @param asset asset
     */
    public abstract Map<String,String> getAssetDetail(Asset asset);

    /**
     * convert to asset id
     * @param market market
     * @param symbol symbol
     * @return asset id
     */
    public String toAssetId(String market, String symbol) {
        return String.format("%s.%s", market, symbol);
    }

}
