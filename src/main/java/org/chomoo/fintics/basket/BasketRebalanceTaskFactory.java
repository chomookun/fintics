package org.chomoo.fintics.basket;

import lombok.RequiredArgsConstructor;
import org.chomoo.fintics.model.Basket;
import org.chomoo.fintics.service.BasketService;
import org.chomoo.fintics.service.TradeService;
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
