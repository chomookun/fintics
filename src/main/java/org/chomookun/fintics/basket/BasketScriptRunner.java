package org.chomookun.fintics.basket;

import ch.qos.logback.classic.Logger;
import lombok.Getter;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.client.ohlcv.OhlcvClient;
import org.chomookun.fintics.model.Basket;
import org.chomookun.fintics.service.AssetService;
import org.chomookun.fintics.strategy.StrategyRunner;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

public abstract class BasketScriptRunner {

    @Getter
    protected final Basket basket;

    @Getter
    protected final AssetService assetService;

    @Getter
    protected final OhlcvClient ohlcvClient;

    protected Logger log = (Logger) LoggerFactory.getLogger(StrategyRunner.class);

    protected BasketScriptRunner(Basket basket, AssetService assetService, OhlcvClient ohlcvClient) {
        this.basket = basket;
        this.assetService = assetService;
        this.ohlcvClient = ohlcvClient;
    }

    /**
     * sets logger
     * @param log
     */
    public void setLog(Logger log) {
        this.log = log;
    }

    /**
     * get basket rebalance results
     * @return rebalance results
     */
    public abstract List<BasketRebalanceAsset> run();

    /**
     * loads properties string to properties object
     * @param propertiesString property string
     * @return properties
     */
    Properties loadRuleConfigAsProperties(String propertiesString) {
        return PbePropertiesUtil.loadProperties(propertiesString);
    }

}
