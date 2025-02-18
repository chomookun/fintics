package org.chomookun.fintics.core.trade.executor;

import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.alarm.AlarmService;
import org.chomookun.fintics.core.asset.AssetService;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.core.ohlcv.OhlcvService;
import org.chomookun.fintics.core.order.OrderService;
import org.chomookun.fintics.core.strategy.runner.StrategyRunnerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

@Component
@RequiredArgsConstructor
public class TradeExecutorFactory {

    private final PlatformTransactionManager transactionManager;

    private final AssetService assetService;

    private final BasketService basketService;

    private final OhlcvService ohlcvService;

    private final OrderService orderService;

    private final AlarmService alarmService;

    private final StrategyRunnerFactory strategyRunnerFactory;

    private final OhlcvCacheManager ohlcvCacheManager;

    /**
     * gets trade executor
     * @return trade executor
     */
    public TradeExecutor getObject() {
        return TradeExecutor.builder()
                .transactionManager(transactionManager)
                .assetService(assetService)
                .basketService(basketService)
                .ohlcvService(ohlcvService)
                .orderService(orderService)
                .alarmService(alarmService)
                .strategyRunnerFactory(strategyRunnerFactory)
                .ohlcvCacheManager(ohlcvCacheManager)
                .build();
    }

}
