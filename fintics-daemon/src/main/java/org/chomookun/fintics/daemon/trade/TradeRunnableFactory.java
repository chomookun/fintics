package org.chomookun.fintics.daemon.trade;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.broker.client.BrokerClientFactory;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.strategy.StrategyService;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.executor.TradeAssetStoreFactory;
import org.chomookun.fintics.core.trade.executor.TradeExecutorFactory;
import org.springframework.stereotype.Component;

/**
 * trade runnable factory
 */
@Component
@RequiredArgsConstructor
public class TradeRunnableFactory {

    private final TradeService tradeService;

    private final StrategyService strategyService;

    private final BrokerService brokerService;

    private final TradeExecutorFactory tradeExecutorFactory;

    private final BrokerClientFactory brokerClientFactory;

    private final TradeAssetStoreFactory tradeAssetStoreFactory;

    /**
     * creates trade runnable
     * @param trade trade info
     * @return trade runnable
     */
    public TradeRunnable getObject(Trade trade) {
        return TradeRunnable.builder()
                .tradeId(trade.getTradeId())
                .interval(trade.getInterval())
                .tradeService(tradeService)
                .strategyService(strategyService)
                .brokerService(brokerService)
                .tradeExecutor(tradeExecutorFactory.getObject())
                .brokerClientFactory(brokerClientFactory)
                .tradeAssetStoreFactory(tradeAssetStoreFactory)
                .build();
    }

}
