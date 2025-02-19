package org.chomookun.fintics.core.basket.rebalance;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.basket.model.Basket;

import java.util.List;

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
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);
        Object result = groovyShell.evaluate(
                "import " + BasketRebalanceAsset.class.getName() + '\n' +
                        basket.getScript()
        );
        if (result != null) {
            return (List<BasketRebalanceAsset>) result;
        }
        return null;
    }

}
