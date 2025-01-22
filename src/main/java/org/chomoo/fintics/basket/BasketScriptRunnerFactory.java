package org.chomoo.fintics.basket;

import lombok.RequiredArgsConstructor;
import org.chomoo.fintics.client.ohlcv.OhlcvClient;
import org.chomoo.fintics.model.Basket;
import org.chomoo.fintics.service.AssetService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasketScriptRunnerFactory {

    private final AssetService assetService;

    private final OhlcvClient ohlcvClient;

    /**
     * gets object
     * @param basket basket
     * @return strategy runner
     */
    public BasketScriptRunner getObject(Basket basket) {
        switch (basket.getLanguage()) {
            case GROOVY -> {
                return GroovyBasketScriptRunner.builder()
                        .basket(basket)
                        .assetService(assetService)
                        .ohlcvClient(ohlcvClient)
                        .build();
            }
            case PYTHON -> {
                return PythonBasketScriptRunner.builder()
                        .basket(basket)
                        .assetService(assetService)
                        .ohlcvClient(ohlcvClient)
                        .build();
            }
            default -> throw new RuntimeException("invalid basket.language");
        }
    }

}
