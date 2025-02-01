package org.chomookun.fintics.client.asset;

import lombok.Getter;
import org.chomookun.fintics.model.Asset;

import java.util.List;

@Getter
public abstract class AssetClient {

    private final AssetClientProperties assetClientProperties;

    /**
     * constructor
     * @param assetClientProperties asset client properties
     */
    protected AssetClient(AssetClientProperties assetClientProperties) {
        this.assetClientProperties = assetClientProperties;
    }

    /**
     * checks support asset
     * @param asset asset
     * @return support or not
     */
    public abstract boolean isSupport(Asset asset);

    /**
     * returns assets
     * @return assets
     */
    public abstract List<Asset> getAssets();

    /**
     * Updates asset
     * @param asset asset
     */
    public abstract void updateAsset(Asset asset);

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
