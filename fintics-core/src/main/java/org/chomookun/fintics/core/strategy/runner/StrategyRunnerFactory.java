package org.chomookun.fintics.core.strategy.runner;

import org.chomookun.fintics.core.strategy.model.Strategy;
import org.springframework.stereotype.Component;

@Component
public class StrategyRunnerFactory {

    /**
     * gets object
     * @param context strategy runner context
     * @return strategy runner
     */
    public StrategyRunner getObject(StrategyRunnerContext context) {
        Strategy strategy = context.getStrategy();
        switch (strategy.getLanguage()) {
            case GROOVY -> {
                return GroovyStrategyRunner.builder()
                        .strategy(context.getStrategy())
                        .variables(context.getVariables())
                        .dateTime(context.getDateTime())
                        .basketAsset(context.getBasketAsset())
                        .tradeAsset(context.getTradeAsset())
                        .balanceAsset(context.getBalanceAsset())
                        .orderBook(context.getOrderBook())
                        .build();
            }
            case PYTHON -> {
                return PythonStrategyRunner.builder()
                        .strategy(context.getStrategy())
                        .variables(context.getVariables())
                        .dateTime(context.getDateTime())
                        .basketAsset(context.getBasketAsset())
                        .tradeAsset(context.getTradeAsset())
                        .balanceAsset(context.getBalanceAsset())
                        .orderBook(context.getOrderBook())
                        .build();
            }
            default -> throw new RuntimeException("invalid strategy.language");
        }
    }

}
