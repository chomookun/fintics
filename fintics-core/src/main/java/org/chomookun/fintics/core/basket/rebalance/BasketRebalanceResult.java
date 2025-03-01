package org.chomookun.fintics.core.basket.rebalance;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.basket.model.BasketAsset;

import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class BasketRebalanceResult {

    @Builder.Default
    private List<BasketRebalanceAsset> basketRebalanceAssets = new ArrayList<>();

    @Builder.Default
    private List<BasketAsset> addedBasketAssets = new ArrayList<>();

    @Builder.Default
    private List<BasketAsset> removedBasketAssets = new ArrayList<>();

    /**
     * Converts basket rebalance result to formatted string
      * @return formatted string
     */
    public String toFormattedString() {
        StringBuilder sb = new StringBuilder();
        // basket rebalance assets
        sb.append(String.format("# [%d]basketRebalanceAssets", basketRebalanceAssets.size())).append('\n');
        basketRebalanceAssets.forEach(it -> {
            sb.append(String.format("- [%s]%s(%s): %s", it.getSymbol(), it.getName(), it.getHoldingWeight().toPlainString(), it.getRemark())).append('\n');
        });
        // added basket assets
        sb.append(String.format("# [%d]addedBasketAsset", addedBasketAssets.size())).append('\n');
        addedBasketAssets.forEach(it -> {
            sb.append(String.format("- [%s]%s(%s)", it.getAssetId(), it.getName(), it.getHoldingWeight().toPlainString())).append('\n');
        });
        // removed basket assets
        sb.append(String.format("# [%d]removedBasketAsset", removedBasketAssets.size())).append('\n');
        removedBasketAssets.forEach(it -> {
            sb.append(String.format("- [%s]%s(%s)", it.getAssetId(), it.getName(), it.getHoldingWeight().toPlainString())).append('\n');
        });
        // returns
        return sb.toString();
    }

}
