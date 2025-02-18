package org.chomookun.fintics.core.basket.rebalance;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.core.trade.TradeService;
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
