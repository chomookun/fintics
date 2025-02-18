package org.chomookun.fintics.core.basket.rebalance;

import lombok.experimental.SuperBuilder;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.chomookun.fintics.core.basket.model.Basket;

import java.util.ArrayList;
import java.util.List;

@SuperBuilder
public class PythonBasketScriptRunner extends BasketScriptRunner {

    @Override
    public List<BasketRebalanceAsset> run() {
        Basket basket = getBasket();
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            List<BasketRebalanceAsset> basketRebalanceAssets = new ArrayList<>();
            Value bindings = context.getBindings("python");
            bindings.putMember("basket", basket);
            bindings.putMember("asset_service", getAssetService());
            bindings.putMember("ohlcv_service", getOhlcvService());
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
