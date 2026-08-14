package org.chomookun.fintics.core.strategy.runner;

import ch.qos.logback.classic.Logger;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.core.broker.model.BalanceAsset;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.strategy.model.Strategy;
import org.chomookun.fintics.core.trade.model.TradeAsset;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Properties;

public abstract class StrategyRunner {

    protected final Strategy strategy;

    protected final String variables;

    protected final LocalDateTime dateTime;

    protected final BasketAsset basketAsset;

    protected final TradeAsset tradeAsset;

    protected final BalanceAsset balanceAsset;

    protected final OrderBook orderBook;

    protected Logger log = (Logger) LoggerFactory.getLogger(StrategyRunner.class);

    /**
     * constructor
     * @param strategy strategy
     * @param variables variable
     * @param dateTime date time
     * @param basketAsset basket asset
     * @param tradeAsset trade asset
     * @param balanceAsset balance asset
     * @param orderBook order book
     */
    public StrategyRunner(
            Strategy strategy,
            String variables,
            LocalDateTime dateTime,
            BasketAsset basketAsset,
            TradeAsset tradeAsset,
            BalanceAsset balanceAsset,
            OrderBook orderBook
    ) {
        this.strategy  = strategy;
        this.variables = variables;
        this.dateTime = dateTime;
        this.basketAsset = basketAsset;
        this.tradeAsset = tradeAsset;
        this.balanceAsset = balanceAsset;
        this.orderBook = orderBook;
    }

    /**
     * sets logger
     * @param log
     */
    public void setLog(Logger log) {
        this.log = log;
    }

    /**
     * runs strategy script
     * @return result of execution
     */
    public abstract StrategyResult run();

    /**
     * loads properties string to properties object
     * @param propertiesString property string
     * @return properties
     */
    Properties loadRuleConfigAsProperties(String propertiesString) {
        return PbePropertiesUtil.loadProperties(propertiesString);
    }

}