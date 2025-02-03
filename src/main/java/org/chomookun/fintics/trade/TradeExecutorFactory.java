package org.chomookun.fintics.trade;

import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.alarm.service.AlarmService;
import org.chomookun.fintics.service.AssetService;
import org.chomookun.fintics.service.BasketService;
import org.chomookun.fintics.service.OhlcvService;
import org.chomookun.fintics.service.OrderService;
import org.chomookun.fintics.strategy.StrategyRunnerFactory;
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
