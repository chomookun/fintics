package org.oopscraft.fintics.thread;

import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.AssetIndicator;
import org.oopscraft.fintics.model.Market;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.slf4j.Logger;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TradeAssetDecider {

    private final String holdCondition;

    private final Logger logger;

    private final LocalDateTime dateTime;

    private final AssetIndicator assetIndicator;

    private final Market market;

    @Builder
    public TradeAssetDecider(String holdCondition, Logger logger, LocalDateTime dateTime, AssetIndicator assetIndicator, Market market) {
        this.holdCondition = holdCondition;
        this.logger = logger;
        this.dateTime = dateTime;
        this.assetIndicator = assetIndicator;
        this.market = market;
    }

    public Boolean execute() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("log", logger);
        binding.setVariable("dateTime", dateTime);
        binding.setVariable("assetIndicator", assetIndicator);
        binding.setVariable("market", market);
        binding.setVariable("tool", new Tool());
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);

        if(holdCondition == null || holdCondition.isBlank()) {
            return null;
        }
        Object result = groovyShell.evaluate(holdCondition);
        if(result == null) {
            return null;
        }
        return (Boolean) result;
    }

}