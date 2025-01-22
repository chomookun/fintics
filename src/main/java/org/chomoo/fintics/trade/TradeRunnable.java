package org.chomoo.fintics.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.chomoo.fintics.client.broker.BrokerClient;
import org.chomoo.fintics.client.broker.BrokerClientFactory;
import org.chomoo.fintics.model.Broker;
import org.chomoo.fintics.model.Strategy;
import org.chomoo.fintics.model.Trade;
import org.chomoo.fintics.service.BrokerService;
import org.chomoo.fintics.service.StrategyService;
import org.chomoo.fintics.service.TradeService;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

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

    private final PlatformTransactionManager transactionManager;

    private final Logger log;

    @Setter
    private LogAppender logAppender;

    @Setter
    @Getter
    private boolean interrupted = false;

    /**
     * constructor
     * @param tradeId trade id
     * @param interval interval(seconds)
     * @param tradeService trade service
     * @param strategyService strategy service
     * @param brokerService broker service
     * @param tradeExecutor trade executor
     * @param brokerClientFactory broker client factory
     * @param tradeAssetStoreFactory trade asset store factory
     * @param transactionManager transaction manager
     */
    @Builder
    protected TradeRunnable(
        String tradeId,
        Integer interval,
        TradeService tradeService,
        StrategyService strategyService,
        BrokerService brokerService,
        TradeExecutor tradeExecutor,
        BrokerClientFactory brokerClientFactory,
        TradeAssetStoreFactory tradeAssetStoreFactory,
        PlatformTransactionManager transactionManager
    ){
        this.tradeId = tradeId;
        this.interval = interval;
        this.tradeService = tradeService;
        this.strategyService = strategyService;
        this.brokerService = brokerService;
        this.tradeExecutor = tradeExecutor;
        this.brokerClientFactory = brokerClientFactory;
        this.tradeAssetStoreFactory = tradeAssetStoreFactory;
        this.transactionManager = transactionManager;

        // log
        this.log = (Logger) LoggerFactory.getLogger(tradeId);
    }

    /**
     * runs trade
     */
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
        TradeAssetStore statusHandler = tradeAssetStoreFactory.getObject(destination, true);
        tradeExecutor.setTradeAssetStore(statusHandler);

        // start loop
        log.info("Start TradeRunnable: {}", tradeId);
        while(!Thread.currentThread().isInterrupted() && !interrupted) {
            TransactionStatus transactionStatus = null;
            try {
                // wait interval
                log.info("Waiting interval: {} seconds", interval);
                Thread.sleep(interval * 1_000);

                // start transaction
                DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
                transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                transactionStatus = transactionManager.getTransaction(transactionDefinition);

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

                // end transaction
                transactionManager.commit(transactionStatus);

            } catch (InterruptedException e) {
                log.warn("TradeRunnable is interrupted.");
                break;
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
            } finally {
                if(transactionStatus != null) {
                    if(!transactionStatus.isCompleted()) {
                        transactionStatus.setRollbackOnly();
                        transactionManager.commit(transactionStatus);
                    }
                }
            }
        }
        log.info("End TradeRunnable: {}", tradeId);
        if (this.logAppender != null) {
            this.logAppender.stop();
            log.detachAppender(this.logAppender);
        }
    }

}
