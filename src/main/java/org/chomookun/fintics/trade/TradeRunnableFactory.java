package org.chomookun.fintics.trade;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.client.broker.BrokerClientFactory;
import org.chomookun.fintics.model.Trade;
import org.chomookun.fintics.service.BrokerService;
import org.chomookun.fintics.service.StrategyService;
import org.chomookun.fintics.service.TradeService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

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
