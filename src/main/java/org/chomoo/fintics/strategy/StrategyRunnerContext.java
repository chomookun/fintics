package org.chomoo.fintics.strategy;

import lombok.Builder;
import lombok.Getter;
import org.chomoo.fintics.model.*;

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
