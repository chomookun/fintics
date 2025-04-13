package org.chomookun.fintics.daemon.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientFactory;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.strategy.model.Strategy;
import org.chomookun.fintics.core.trade.executor.TradeLogAppender;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.strategy.StrategyService;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.executor.TradeAssetStore;
import org.chomookun.fintics.core.trade.executor.TradeAssetStoreFactory;
import org.chomookun.fintics.core.trade.executor.TradeExecutor;
import org.slf4j.LoggerFactory;

import java.time.*;

public class TradeRunnable implements Runnable {

    @Getter
    private final String tradeId;

    @Getter
    private final Integer interval;

    private final TradeService tradeService;

    private final StrategyService strategyService;

    private final BrokerService brokerService;

    private final TradeExecutor tradeExecutor;

    private final BrokerClientFactory brokerClientFactory;

    private final TradeAssetStoreFactory tradeAssetStoreFactory;

    private final Logger log;

    @Setter
    private TradeLogAppender logAppender;

    @Setter
    @Getter
    private boolean interrupted = false;

    @Builder
    protected TradeRunnable(
        String tradeId,
        Integer interval,
        TradeService tradeService,
        StrategyService strategyService,
        BrokerService brokerService,
        TradeExecutor tradeExecutor,
        BrokerClientFactory brokerClientFactory,
        TradeAssetStoreFactory tradeAssetStoreFactory
    ){
        this.tradeId = tradeId;
        this.interval = interval;
        this.tradeService = tradeService;
        this.strategyService = strategyService;
        this.brokerService = brokerService;
        this.tradeExecutor = tradeExecutor;
        this.brokerClientFactory = brokerClientFactory;
        this.tradeAssetStoreFactory = tradeAssetStoreFactory;
        // log
        this.log = (Logger) LoggerFactory.getLogger(tradeId);
    }

    @Override
    public void run() {
        // logger
        tradeExecutor.setLog(log);
        if (this.logAppender != null) {
            log.addAppender(this.logAppender);
            this.logAppender.start();
        }
        // status template
        String destination = String.format("/trades/%s/assets", tradeId);
        TradeAssetStore statusHandler = tradeAssetStoreFactory.getObject();
        tradeExecutor.setTradeAssetStore(statusHandler);
        // start loop
        log.info("Start TradeRunnable: {}", tradeId);
        while(!Thread.currentThread().isInterrupted() && !interrupted) {
            Instant tradeStartTime = Instant.now();
            try {
                // wait interval
                log.info("Waiting interval: {} seconds", interval);
                Thread.sleep(interval * 1_000);

                // call trade executor
                Trade trade = tradeService.getTrade(tradeId).orElseThrow();
                Strategy strategy = strategyService.getStrategy(trade.getStrategyId()).orElseThrow();
                Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
                BrokerClient brokerClient = brokerClientFactory.getObject(broker);
                ZoneId timezone = brokerClient.getDefinition().getTimezone();
                LocalDateTime dateTime = Instant.now()
                        .atZone(timezone)
                        .toLocalDateTime();
                tradeExecutor.execute(trade, strategy, dateTime, brokerClient);

            } catch (InterruptedException e) {
                log.warn("TradeRunnable is interrupted.");
                break;
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            } finally {
                log.info("Trade elapsed time: {}",  Duration.between(tradeStartTime, Instant.now()));
            }
        }
        log.info("End TradeRunnable: {}", tradeId);
        if (this.logAppender != null) {
            this.logAppender.stop();
            log.detachAppender(this.logAppender);
        }
    }

}
