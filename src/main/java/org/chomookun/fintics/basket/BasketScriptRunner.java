package org.chomookun.fintics.basket;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.model.Basket;
import org.chomookun.fintics.service.AssetService;
import org.chomookun.fintics.service.OhlcvService;
import org.chomookun.fintics.strategy.StrategyRunner;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Properties;

@SuperBuilder
@Getter
public abstract class BasketScriptRunner {

    private final Basket basket;

    private final AssetService assetService;

    private final OhlcvService ohlcvService;

    @Builder.Default
    protected Logger log = (Logger) LoggerFactory.getLogger(StrategyRunner.class);

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
