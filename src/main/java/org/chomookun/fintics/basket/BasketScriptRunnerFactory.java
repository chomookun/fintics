package org.chomookun.fintics.basket;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.client.ohlcv.OhlcvClient;
import org.chomookun.fintics.model.Basket;
import org.chomookun.fintics.service.AssetService;
import org.chomookun.fintics.service.OhlcvService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasketScriptRunnerFactory {

    private final AssetService assetService;

    private final OhlcvService ohlcvService;

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
                        .ohlcvService(ohlcvService)
                        .build();
            }
            case PYTHON -> {
                return PythonBasketScriptRunner.builder()
                        .basket(basket)
                        .assetService(assetService)
                        .ohlcvService(ohlcvService)
                        .build();
            }
            default -> throw new RuntimeException("invalid basket.language");
        }
    }

}
