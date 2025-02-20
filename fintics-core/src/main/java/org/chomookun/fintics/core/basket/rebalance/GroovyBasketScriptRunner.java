package org.chomookun.fintics.core.basket.rebalance;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.asset.model.AssetSearch;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.indicator.*;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.core.strategy.runner.StrategyResult;
import org.chomookun.fintics.core.trade.model.TradeAsset;

import java.awt.print.Pageable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuperBuilder
public class GroovyBasketScriptRunner extends BasketScriptRunner {

    @Override
    @SuppressWarnings("unchecked")
    public List<BasketRebalanceAsset> run() {
        Basket basket = getBasket();
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("variables", loadRuleConfigAsProperties(basket.getVariables()));
        binding.setVariable("basket", basket);
        binding.setVariable("assetService", getAssetService());
        binding.setVariable("ohlcvService", getOhlcvService());
        binding.setVariable("log", log);
        String scriptText = getImportClause() + "\n" + basket.getScript();
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);
        Object result = groovyShell.evaluate(scriptText);
        if (result != null) {
            return (List<BasketRebalanceAsset>) result;
        }
        return null;
    }

    static String getImportClause() {
        Set<String> importPaths = new HashSet<>();
        // models
        importPaths.add(Asset.class.getName());
        importPaths.add(AssetSearch.class.getName());
        importPaths.add(Basket.class.getName());
        importPaths.add(BasketAsset.class.getName());
        importPaths.add(Ohlcv.class.getName());
        importPaths.add(BasketRebalanceAsset.class.getName());
        importPaths.add(BasketRebalanceResult.class.getName());
        importPaths.add(Pageable.class.getName());
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
