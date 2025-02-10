package org.chomookun.fintics.basket;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.client.ohlcv.OhlcvClient;
import org.chomookun.fintics.model.Basket;
import org.chomookun.fintics.service.AssetService;
import org.chomookun.fintics.service.OhlcvService;

import java.util.List;

@SuperBuilder
public class GroovyBasketScriptRunner extends BasketScriptRunner {

//    @Builder
//    public GroovyBasketScriptRunner(Basket basket, AssetService assetService, OhlcvService ohlcvService) {
//        super(basket, assetService, ohlcvClient);
//    }

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
