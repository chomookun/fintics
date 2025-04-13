package org.chomookun.fintics.core.strategy.runner;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.broker.model.BalanceAsset;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.ohlcv.indicator.*;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.strategy.model.Strategy;
import org.chomookun.fintics.core.trade.model.TradeAsset;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class GroovyStrategyRunner extends StrategyRunner {

    @Builder
    public GroovyStrategyRunner(Strategy strategy, String variables, LocalDateTime dateTime, BasketAsset basketAsset, TradeAsset tradeAsset, BalanceAsset balanceAsset, OrderBook orderBook) {
        super(strategy, variables, dateTime, basketAsset, tradeAsset, balanceAsset, orderBook);
    }

    @Override
    public StrategyResult run() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        try (GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader)) {
            Binding binding = new Binding();
            binding.setVariable("variables", loadRuleConfigAsProperties(variables));
            binding.setVariable("log", log);
            binding.setVariable("dateTime", dateTime);
            binding.setVariable("basketAsset", basketAsset);
            binding.setVariable("tradeAsset", tradeAsset);
            binding.setVariable("balanceAsset", balanceAsset);
            binding.setVariable("orderBook", orderBook);
            String scriptText = getImportClause() + "\n" + strategy.getScript();
            GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);
            Object result = groovyShell.evaluate(scriptText);
            if (result != null) {
                return (StrategyResult) result;
            }
            return null;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    static String getImportClause() {
        Set<String> importPaths = new HashSet<>();
        // models
        importPaths.add(Asset.class.getName());
        importPaths.add(Basket.class.getName());
        importPaths.add(BasketAsset.class.getName());
        importPaths.add(Ohlcv.class.getName());
        importPaths.add(TradeAsset.class.getName());
        importPaths.add(StrategyResult.class.getName());
        importPaths.add(StrategyResult.Action.class.getName());
        // indicator
        importPaths.add(Tools.class.getPackageName() + ".*");
        importPaths.add(Indicator.class.getPackageName() + ".*");
        importPaths.add(IndicatorContext.class.getPackageName() + ".*");
        importPaths.add(IndicatorCalculator.class.getPackageName() + ".*");
        IndicatorCalculatorFactory.getRegistry().forEach((context, calculator) -> {
            importPaths.add(context.getPackageName() + ".*");
            importPaths.add(calculator.getPackageName() + ".*");
        });
        // returns as import clause
        return importPaths.stream()
                .map(it -> it.replaceAll("\\$", "."))   // inner class
                .map(it -> "import " + it + ";\n")
                .collect(Collectors.joining());
    }

}
