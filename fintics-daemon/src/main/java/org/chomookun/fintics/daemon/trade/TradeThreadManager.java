package org.chomookun.fintics.daemon.trade;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.core.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.trade.executor.TradeLogAppender;
import org.chomookun.fintics.core.trade.executor.TradeLogAppenderFactory;
import org.jetbrains.annotations.NotNull;
import org.chomookun.fintics.core.trade.model.Trade;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * trade thread manager
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TradeThreadManager implements ApplicationListener<ContextStoppedEvent> {

    private final TradeRunnableFactory tradeRunnableFactory;

    private final ThreadGroup tradeThreadGroup = new ThreadGroup("trade");

    private final Map<String,TradeThread> tradeThreadMap = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    private final TradeLogAppenderFactory tradeLogAppenderFactory;

    /**
     * starts trade thread
     * @param trade trade
     */
    public synchronized void startTradeThread(Trade trade) {
        synchronized (this) {
            log.info("Start TradeThread - {}", trade.getTradeId());

            // check already running
            if(isTradeThreadRunning(trade.getTradeId())) {
                throw new RuntimeException(String.format("Thread Thread[%s] is already running.", trade.getTradeId()));
            }

            // trade runnable
            TradeRunnable tradeRunnable = tradeRunnableFactory.getObject(trade);
            Context context = ((Logger)log).getLoggerContext();
            TradeLogAppender tradeLogAppender = tradeLogAppenderFactory.getObject(context, trade);
            tradeRunnable.setLogAppender(tradeLogAppender);

            // run thread
            TradeThread tradeThread = new TradeThread(tradeThreadGroup, tradeRunnable, trade.getTradeId());
            tradeThread.setDaemon(true);
            tradeThread.start();
            tradeThreadMap.put(trade.getTradeId(), tradeThread);
        }
    }

    /**
     * stop trade thread
     * @param id thread id
     */
    public synchronized void stopTradeThread(String id) {
        synchronized (this) {
            log.info("Stop Trade Thread - {}", id);

            // checks target exists
            if(!isTradeThreadRunning(id)) {
                throw new RuntimeException(String.format("Thread Thread[%s] is not running.", id));
            }

            // interrupt thread
            getTradeThread(id).ifPresent(tradeThread -> {
                try {
                    tradeThread.interrupt();
                    tradeThread.join(60_000);
                    tradeThreadMap.remove(id);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                    throw new RuntimeException(e);
                }
            });
        }
    }

    /**
     * restarts trade thread
     * @param trade trade
     */
    public synchronized void restartTradeThread(Trade trade) {
        if(isTradeThreadRunning(trade.getTradeId())) {
            stopTradeThread(trade.getTradeId());
        }
        startTradeThread(trade);
    }

    /**
     * returns trade threads
     * @return list of trade thread
     */
    public List<TradeThread> getTradeThreads() {
        return new ArrayList<>(tradeThreadMap.values());
    }

    /**
     * return specific trade thread
     * @param id trade thread id
     * @return trade thread
     */
    public Optional<TradeThread> getTradeThread(String id) {
        if(tradeThreadMap.containsKey(id)) {
            return Optional.of(tradeThreadMap.get(id));
        }else{
            return Optional.empty();
        }
    }

    /**
     * checks trade thread is running
     * @param id thread tread id
     * @return whether thread is running
     */
    public boolean isTradeThreadRunning(String id) {
        return getTradeThread(id).isPresent();
    }

    /**
     * handles application stop event
     * @param event application event
     */
    @Override
    public void onApplicationEvent(@NotNull ContextStoppedEvent event) {
        log.info("Shutdown all trade trade.[{}]", event);
        tradeThreadGroup.interrupt();
    }

}
