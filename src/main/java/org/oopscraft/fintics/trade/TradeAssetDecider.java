package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import groovy.lang.Binding;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyShell;
import lombok.Builder;
import org.oopscraft.fintics.model.*;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TradeAssetDecider {

    private final String holdCondition;

    private final Logger log;

    private final LocalDateTime dateTime;

    private final OrderBook orderBook;

    private final Balance balance;

    private final Map<String, IndiceIndicator> indiceIndicators;

    private final AssetIndicator assetIndicator;

    @Builder
    protected TradeAssetDecider(String holdCondition, Logger log, LocalDateTime dateTime, OrderBook orderBook, Balance balance, List<IndiceIndicator> indiceIndicators, AssetIndicator assetIndicator) {
        this.holdCondition = holdCondition;
        this.log = log;
        this.dateTime = dateTime;
        this.orderBook = orderBook;
        this.balance = balance;
        this.indiceIndicators = indiceIndicators.stream()
                .collect(Collectors.toMap(indiceIndicator ->
                        indiceIndicator.getId().name(), indiceIndicator -> indiceIndicator));
        this.assetIndicator = assetIndicator;
    }

    public Boolean execute() {
        ClassLoader classLoader = this.getClass().getClassLoader();
        GroovyClassLoader groovyClassLoader = new GroovyClassLoader(classLoader);
        Binding binding = new Binding();
        binding.setVariable("log", log);
        binding.setVariable("dateTime", dateTime);
        binding.setVariable("orderBook", orderBook);
        binding.setVariable("balance", balance);
        binding.setVariable("indiceIndicators", indiceIndicators);
        binding.setVariable("assetIndicator", assetIndicator);
        binding.setVariable("tool", new Tool());
        GroovyShell groovyShell = new GroovyShell(groovyClassLoader, binding);

        if(holdCondition == null || holdCondition.isBlank()) {
            return null;
        }
        Instant startTime = Instant.now();
        Object result = groovyShell.evaluate(holdCondition);
        log.info("Elapsed:{}", Duration.between(startTime, Instant.now()));
        if(result == null) {
            return null;
        }
        return (Boolean) result;
    }
}