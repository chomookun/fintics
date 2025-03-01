package org.chomookun.fintics.core.asset.client;

import lombok.Getter;
import org.chomookun.fintics.core.asset.model.Asset;

import java.util.List;

@Getter
public abstract class AssetClient {

    private final AssetClientProperties assetClientProperties;

    /**
     * Constructor
     * @param assetClientProperties asset client properties
     */
    protected AssetClient(AssetClientProperties assetClientProperties) {
        this.assetClientProperties = assetClientProperties;
    }

    /**
     * Checks support asset
     * @param asset asset
     * @return support or not
     */
    public abstract boolean isSupport(Asset asset);

    /**
     * Returns assets
     * @return assets
     */
    public abstract List<Asset> getAssets();

    /**
     * Populates asset
     * @param asset asset
     */
    public abstract void populateAsset(Asset asset);

    /**
     * Convert to asset id
     * @param market market
     * @param symbol symbol
     * @return asset id
     */
    public String toAssetId(String market, String symbol) {
        return String.format("%s.%s", market, symbol);
    }

}
