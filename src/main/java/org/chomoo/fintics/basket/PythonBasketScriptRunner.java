package org.chomoo.fintics.basket;

import lombok.Builder;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.chomoo.fintics.client.ohlcv.OhlcvClient;
import org.chomoo.fintics.model.Basket;
import org.chomoo.fintics.service.AssetService;

import java.util.ArrayList;
import java.util.List;

public class PythonBasketScriptRunner extends BasketScriptRunner {

    @Builder
    public PythonBasketScriptRunner(Basket basket, AssetService assetService, OhlcvClient ohlcvClient) {
        super(basket, assetService, ohlcvClient);
    }

    @Override
    public List<BasketRebalanceAsset> run() {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            List<BasketRebalanceAsset> basketRebalanceAssets = new ArrayList<>();
            Value bindings = context.getBindings("python");
            bindings.putMember("basket", basket);
            bindings.putMember("asset_service", assetService);
            bindings.putMember("ohlcv_client", ohlcvClient);
            bindings.putMember("basket_rebalance_assets", basketRebalanceAssets);
            bindings.putMember("log", log);
            context.eval("python",
                    basket.getScript()
            );
            Value strategyResultValue = bindings.getMember("basket_rebalance_assets");
            if (!strategyResultValue.isNull()) {
                return basketRebalanceAssets;
            }
            return null;
        }
    }

}
