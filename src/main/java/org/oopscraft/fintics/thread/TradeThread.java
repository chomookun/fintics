package org.oopscraft.fintics.thread;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.Client;
import org.oopscraft.fintics.client.ClientFactory;
import org.oopscraft.fintics.model.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalTime;
import java.util.List;

@Slf4j
public class TradeThread extends Thread {

    @Getter
    private final Trade trade;

    @Getter
    private final TradeLogAppender tradeLogAppender;

    private final AlarmService alarmService;

    private final Client client;

    @Getter
    private Balance balance;

    private boolean terminated;

    @Builder
    public TradeThread(Trade trade, TradeLogAppender tradeLogAppender, AlarmService alarmService) {
        this.trade = trade;
        this.tradeLogAppender = tradeLogAppender;
        this.alarmService = alarmService;

        // add log appender
        ((Logger)log).addAppender(this.tradeLogAppender);

        // creates client
        this.client = ClientFactory.getClient(trade.getClientType(), trade.getClientProperties());
    }

    public void terminate() {
        this.terminated = true;
        this.interrupt();
        try {
            this.join();
            this.tradeLogAppender.stop();
        } catch (InterruptedException e) {
            log.warn(e.getMessage());
        }
    }

    @Override
    public void run() {
        while(!this.isInterrupted() && !terminated) {
            try {
                sleepMillis(trade.getInterval() * 1_000);

                // checks start,end time
                if (!isOperatingTime(LocalTime.now())) {
                    log.info("Not operating time - [{}] {} ~ {}", trade.getName(), trade.getStartAt(), trade.getEndAt());
                    continue;
                }
                log.info("Check trade - [{}]", trade.getName());

                // balance
                balance = client.getBalance();

                // checks buy condition
                for (TradeAsset tradeAsset : trade.getTradeAssets()) {

                    // check enabled
                    if (!tradeAsset.isEnabled()) {
                        continue;
                    }

                    // force delay
                    sleepMillis(1000);

                    // logging
                    log.info("Check asset - [{}]", tradeAsset.getName());

                    // build asset indicator
                    OrderBook orderBook = client.getOrderBook(tradeAsset);
                    List<Ohlcv> minuteOhlcvs = client.getMinuteOhlcvs(tradeAsset);
                    List<Ohlcv> dailyOhlcvs = client.getDailyOhlcvs(tradeAsset);
                    AssetIndicator assetIndicator = AssetIndicator.builder()
                            .asset(tradeAsset)
                            .orderBook(orderBook)
                            .minuteOhlcvs(minuteOhlcvs)
                            .dailyOhlcvs(dailyOhlcvs)
                            .build();

                    // decides hold condition
                    TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                            .holdCondition(trade.getHoldCondition())
                            .assetIndicator(assetIndicator)
                            .logger(log)
                            .build();
                    Boolean holdConditionResult = tradeAssetDecider.execute();

                    // 1. null is no operation
                    if (holdConditionResult == null) {
                        continue;
                    }

                    // 2. buy and hold
                    if (holdConditionResult.equals(Boolean.TRUE)) {
                        if (!balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            BigDecimal buyAmount = BigDecimal.valueOf(balance.getTotalAmount())
                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                                    .multiply(BigDecimal.valueOf(tradeAsset.getHoldRatio()));
                            Double askPrice = assetIndicator.getOrderBook().getAskPrice();
                            int quantity = buyAmount
                                    .divide(BigDecimal.valueOf(askPrice), 0, RoundingMode.FLOOR)
                                    .intValue();
                            try {
                                client.buyAsset(tradeAsset, quantity);
                                sendBuyOrderAlarmIfEnabled(tradeAsset, quantity);
                            } catch (Throwable e) {
                                log.warn(e.getMessage());
                                sendErrorAlarmIfEnabled(e);
                            }
                        }
                    }

                    // 3. sell
                    else if (holdConditionResult.equals(Boolean.FALSE)) {
                        if (balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                            BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getSymbol());
                            Integer quantity = balanceAsset.getQuantity();
                            try {
                                client.sellAsset(balanceAsset, quantity);
                                sendSellOrderAlarmIfEnabled(balanceAsset, quantity);
                            } catch (Throwable e) {
                                log.warn(e.getMessage());
                                sendErrorAlarmIfEnabled(e);
                            }
                        }
                    }
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                sendErrorAlarmIfEnabled(e);
            }
        }
    }

    private void sleepMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) { }
    }

    private boolean isOperatingTime(LocalTime time) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return false;
        }
        return time.isAfter(trade.getStartAt()) && time.isBefore(trade.getEndAt());
    }

    private void sendAlarmIfEnabled(String subject, String content) {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            alarmService.sendAlarm(trade.getAlarmId(), subject, content);
        }
    }

    private void sendErrorAlarmIfEnabled(Throwable t) {
        if(trade.isAlarmOnError()) {
            sendAlarmIfEnabled(t.getMessage(), ExceptionUtils.getStackTrace(t));
        }
    }

    private void sendOrderAlarmIfEnabled(String subject, String content) {
        if(trade.isAlarmOnOrder()) {
            sendAlarmIfEnabled(subject, content);
        }
    }

    private void sendBuyOrderAlarmIfEnabled(TradeAsset tradeAsset, int quantity) {
        String subject = String.format("Buy [%s], %d", tradeAsset.getName(), quantity);
        sendOrderAlarmIfEnabled(subject, null);
    }

    private void sendSellOrderAlarmIfEnabled(BalanceAsset balanceAsset, int quantity) {
        String subject = String.format("Sell [%s], %d", balanceAsset.getName(), quantity);
        sendOrderAlarmIfEnabled(subject, null);
    }

}
