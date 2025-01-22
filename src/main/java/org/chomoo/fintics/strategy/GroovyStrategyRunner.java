package org.chomoo.fintics.strategy;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import org.chomoo.fintics.model.*;

import java.time.LocalDateTime;

public class GroovyStrategyRunner extends StrategyRunner {

    /**
     * constructor
     * @param strategy     strategy
     * @param variables    variable
     * @param dateTime     date time
     * @param basketAsset basket asset
     * @param tradeAsset   trade asset
     * @param balanceAsset balance asset
     * @param orderBook    order book
     */
    @Builder
    public GroovyStrategyRunner(Strategy strategy, String variables, LocalDateTime dateTime, BasketAsset basketAsset, TradeAsset tradeAsset, BalanceAsset balanceAsset, OrderBook orderBook) {
        super(strategy, variables, dateTime, basketAsset, tradeAsset, balanceAsset, orderBook);
    }

    /**
     * executes strategy script
     * @return result of execution
     */
    @Override
    public StrategyResult run() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("variables", loadRuleConfigAsProperties(variables));
        binding.setVariable("log", log);
        binding.setVariable("dateTime", dateTime);
        binding.setVariable("basketAsset", basketAsset);
        binding.setVariable("tradeAsset", tradeAsset);
        binding.setVariable("balanceAsset", balanceAsset);
        binding.setVariable("orderBook", orderBook);
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);
        Object result = groovyShell.evaluate(
                "import " + StrategyResult.class.getName() + '\n' +
                        "import " + StrategyResult.Action.class.getName().replaceAll("\\$",".") + '\n' +
                        strategy.getScript()
        );
        if (result != null) {
            return (StrategyResult) result;
        }
        return null;
    }

}
