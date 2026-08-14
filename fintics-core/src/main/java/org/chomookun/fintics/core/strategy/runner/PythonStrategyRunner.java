package org.chomookun.fintics.core.strategy.runner;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import org.chomookun.fintics.core.broker.model.BalanceAsset;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.strategy.model.Strategy;
import org.chomookun.fintics.core.trade.model.TradeAsset;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Value;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public class PythonStrategyRunner extends StrategyRunner {

    /**
     * constructor
     *
     * @param strategy     strategy
     * @param variables    variable
     * @param dateTime     date time
     * @param basketAsset basket asset
     * @param tradeAsset   trade asset
     * @param balanceAsset balance asset
     * @param orderBook    order book
     */
    @Builder
    public PythonStrategyRunner(Strategy strategy, String variables, LocalDateTime dateTime, BasketAsset basketAsset, TradeAsset tradeAsset, BalanceAsset balanceAsset, OrderBook orderBook) {
        super(strategy, variables, dateTime, basketAsset, tradeAsset, balanceAsset, orderBook);
    }

    @Override
    public StrategyResult run() {
        try (Context context = Context.newBuilder("python")
                .allowAllAccess(true)
                .build()) {
            Value bindings = context.getBindings("python");
            bindings.putMember("variables", variables);
            bindings.putMember("log", log);
            bindings.putMember("date_time", dateTime);
            bindings.putMember("basket_asset", basketAsset);
            bindings.putMember("trade_asset", tradeAsset);
            bindings.putMember("balance_asset", balanceAsset);
            bindings.putMember("order_book", orderBook);
            bindings.putMember("strategy_result", null);
            context.eval("python",
                    strategy.getScript()
            );
            Value strategyResultValue = bindings.getMember("strategy_result");
            if (!strategyResultValue.isNull()) {
                Map<String, Object> strategyResultMap;
                try {
                    // TODO graalvm 에서 type casting 이 제대로 안됨. 일단 땜빵.
                    strategyResultMap = new ObjectMapper().readValue(strategyResultValue.toString().replaceAll("'", "\""), new TypeReference<>() {});
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
                log.info("strategyResultMap:{}", strategyResultMap);
                StrategyResult.Action action = StrategyResult.Action.valueOf(strategyResultMap.get("action").toString());
                BigDecimal position = new BigDecimal(strategyResultMap.get("position").toString());
                String description = Optional.ofNullable(strategyResultMap.get("description"))
                        .map(Object::toString)
                        .orElse(null);
                return StrategyResult.builder()
                        .action(action)
                        .position(position)
                        .description(description)
                        .build();
            }
            return null;
        }
    }

}
