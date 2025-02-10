package org.chomookun.fintics.basket;

import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;
import org.chomookun.fintics.client.ohlcv.OhlcvClient;
import org.chomookun.fintics.model.Basket;
import org.chomookun.fintics.service.AssetService;

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
