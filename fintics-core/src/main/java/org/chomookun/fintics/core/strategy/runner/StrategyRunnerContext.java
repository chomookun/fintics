package org.chomookun.fintics.core.strategy.runner;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.broker.model.BalanceAsset;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.strategy.model.Strategy;
import org.chomookun.fintics.core.trade.model.TradeAsset;

import java.time.LocalDateTime;

@Builder
@Getter
public class StrategyRunnerContext {

    private final Strategy strategy;

    private final String variables;

    private final LocalDateTime dateTime;

    private final BasketAsset basketAsset;

    private final TradeAsset tradeAsset;

    private final BalanceAsset balanceAsset;

    private final OrderBook orderBook;

}
