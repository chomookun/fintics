package org.chomookun.fintics.core.basket.rebalance;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.asset.AssetService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasketScriptRunnerFactory {

    private final AssetService assetService;

    /**
     * Gets object
     * @param basket basket
     * @return strategy runner
     */
    public BasketScriptRunner getObject(Basket basket) {
        switch (basket.getLanguage()) {
            case GROOVY -> {
                return GroovyBasketScriptRunner.builder()
                        .basket(basket)
                        .assetService(assetService)
                        .build();
            }
            case PYTHON -> {
                return PythonBasketScriptRunner.builder()
                        .basket(basket)
                        .assetService(assetService)
                        .build();
            }
            default -> throw new IllegalArgumentException(String.format("invalid basket.language:%s", basket.getLanguage()));
        }
    }

}
