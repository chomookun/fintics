package org.chomookun.fintics.basket;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.model.Basket;
import org.chomookun.fintics.service.BasketService;
import org.chomookun.fintics.service.TradeService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BasketRebalanceTaskFactory {

    private final BasketService basketService;

    private final TradeService tradeService;

    private final BasketScriptRunnerFactory basketScriptRunnerFactory;

    public BasketRebalanceTask getObject(Basket basket) {
        return BasketRebalanceTask.builder()
                .basket(basket)
                .basketService(basketService)
                .tradeService(tradeService)
                .basketScriptRunnerFactory(basketScriptRunnerFactory)
                .build();

    }

}
