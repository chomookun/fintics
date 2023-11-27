package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.arch4j.web.support.SseLogAppender;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TradeRunnable implements Runnable {

    @Getter
    private final String tradeId;

    @Getter
    private final Integer interval;

    @Getter
    private final SseLogAppender sseLogAppender;

    private final PlatformTransactionManager transactionManager;

    private final AlarmService alarmService;

    private final IndiceClient indiceClient;

    private final TradeRepository tradeRepository;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final TradeAssetOhlcvRepository tradeAssetOhlcvRepository;

    private final OrderRepository orderRepository;

    private final Logger log;

    @Setter
    @Getter
    private boolean interrupted = false;

    @Builder
    public TradeRunnable(String tradeId, Integer interval, ApplicationContext applicationContext, SseLogAppender sseLogAppender) {
        this.tradeId = tradeId;
        this.interval = interval;
        this.sseLogAppender = sseLogAppender;

        // component
        this.transactionManager = applicationContext.getBean(PlatformTransactionManager.class);
        this.alarmService = applicationContext.getBean(AlarmService.class);
        this.indiceClient = applicationContext.getBean(IndiceClient.class);
        this.tradeRepository = applicationContext.getBean(TradeRepository.class);
        this.indiceOhlcvRepository = applicationContext.getBean(IndiceOhlcvRepository.class);
        this.tradeAssetOhlcvRepository = applicationContext.getBean(TradeAssetOhlcvRepository.class);
        this.orderRepository = applicationContext.getBean(OrderRepository.class);

        // add log appender
        log = (Logger) LoggerFactory.getLogger(tradeId);
        log.addAppender(this.sseLogAppender);
    }

    @Override
    public void run() {
        this.sseLogAppender.start();
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

                // execute trade
                executeTrade();

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
        this.sseLogAppender.stop();
    }

    private void executeTrade() throws InterruptedException {
        // trade info
        Trade trade = tradeRepository.findById(tradeId)
                .map(Trade::from)
                .orElseThrow();
        try {
            // client
            TradeClient tradeClient = TradeClientFactory.getClient(trade);

            // checks start,end time
            LocalDateTime dateTime = LocalDateTime.now();
            if (!isOperatingTime(trade, dateTime.toLocalTime())) {
                log.info("Not operating time - [{}] {} ~ {}", trade.getName(), trade.getStartAt(), trade.getEndAt());
                return;
            }

            // logging
            log.info("Check trade - [{}]", trade.getName());

            // indice indicators
            List<IndiceIndicator> indiceIndicators = new ArrayList<>();
            for(IndiceSymbol symbol : IndiceSymbol.values()) {
                indiceClient.getMinuteOhlcvs(symbol);

                // minute ohlcvs
                List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(symbol);
                LocalDateTime lastMinute = minuteOhlcvs.get(minuteOhlcvs.size()-1).getDateTime();
                List<Ohlcv> previousMinuteOhlcvs = getIndiceOhlcvs(symbol, OhlcvType.MINUTE, lastMinute.minusMinutes(360), lastMinute.minusMinutes(1));
                minuteOhlcvs.addAll(previousMinuteOhlcvs);

                // daily ohlcvs
                List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(symbol);
                LocalDateTime lastDay = dailyOhlcvs.get(dailyOhlcvs.size()-1).getDateTime();
                List<Ohlcv> previousDailyOhlcvs = getIndiceOhlcvs(symbol, OhlcvType.DAILY, lastDay.minusDays(180), lastDay.minusDays(1));
                dailyOhlcvs.addAll(previousDailyOhlcvs);

                // add indicator
                indiceIndicators.add(IndiceIndicator.builder()
                        .symbol(symbol)
                        .minuteOhlcvs(minuteOhlcvs)
                        .dailyOhlcvs(dailyOhlcvs)
                        .build());
            }

            // balance
            Balance balance = tradeClient.getBalance();

            // checks buy condition
            for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                Thread.sleep(100);

                // check enabled
                if (!tradeAsset.isEnabled()) {
                    continue;
                }

                // logging
                log.info("Check asset - [{}]", tradeAsset.getName());

                // indicator
                List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset);
                LocalDateTime lastMinute = minuteOhlcvs.get(minuteOhlcvs.size()-1).getDateTime();
                List<Ohlcv> previousMinuteOhlcvs = getTradeAssetOhlcvs(tradeAsset, OhlcvType.MINUTE, lastMinute.minusMinutes(360), lastMinute.minusMinutes(1));
                minuteOhlcvs.addAll(previousMinuteOhlcvs);

                List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset);
                LocalDateTime lastDay = dailyOhlcvs.get(dailyOhlcvs.size()-1).getDateTime();
                List<Ohlcv> previousDailyOhlcvs = getTradeAssetOhlcvs(tradeAsset, OhlcvType.DAILY, lastDay.minusDays(180), lastDay.minusDays(1));
                dailyOhlcvs.addAll(previousDailyOhlcvs);

                TradeAssetIndicator assetIndicator = TradeAssetIndicator.builder()
                        .symbol(tradeAsset.getSymbol())
                        .name(tradeAsset.getName())
                        .minuteOhlcvs(minuteOhlcvs)
                        .dailyOhlcvs(dailyOhlcvs)
                        .build();

                // order book
                OrderBook orderBook = tradeClient.getOrderBook(tradeAsset);

                // executes trade asset decider
                TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                        .holdCondition(trade.getHoldCondition())
                        .logger(log)
                        .dateTime(dateTime)
                        .orderBook(orderBook)
                        .assetIndicator(assetIndicator)
                        .indiceIndicators(indiceIndicators)
                        .build();
                Boolean holdConditionResult = tradeAssetDecider.execute();
                log.info("holdConditionResult: {}", holdConditionResult);

                // 1. null is no operation
                if (holdConditionResult == null) {
                    continue;
                }

                // 2. buy and hold
                if (holdConditionResult.equals(Boolean.TRUE)) {
                    if (!balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                        BigDecimal buyAmount = balance.getTotalAmount()
                                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                                .multiply(tradeAsset.getHoldRatio())
                                .setScale(2, RoundingMode.HALF_UP);
                        BigDecimal askPrice = orderBook.getAskPrice();
                        BigDecimal quantity = buyAmount
                                .divide(askPrice, MathContext.DECIMAL32);
                        log.info("Buy asset: {}", tradeAsset.getName());
                        buyTradeAsset(trade, tradeAsset, OrderType.MARKET, quantity, orderBook.getAskPrice());
                    }
                }

                // 3. sell
                else if (holdConditionResult.equals(Boolean.FALSE)) {
                    if (balance.hasBalanceAsset(tradeAsset.getSymbol())) {
                        BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getSymbol()).orElseThrow();
                        BigDecimal orderableQuantity = balanceAsset.getOrderableQuantity();
                        log.info("Sell asset: {}", tradeAsset.getName());
                        sellBalanceAsset(trade, balanceAsset, OrderType.MARKET, orderableQuantity, orderBook.getBidPrice());
                    }
                }
            }
        } catch(InterruptedException e) {
            throw e;
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            sendErrorAlarmIfEnabled(trade, e);
            throw e;
        }
    }

    private boolean isOperatingTime(Trade trade, LocalTime time) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return false;
        }
        return time.isAfter(trade.getStartAt()) && time.isBefore(trade.getEndAt());
    }

    private List<Ohlcv> getIndiceOhlcvs(IndiceSymbol symbol, OhlcvType ohlcvType, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        List<IndiceOhlcvEntity> indiceOhlcvEntities = indiceOhlcvRepository.findAllBySymbolAndOhlcvType(symbol, ohlcvType, dateTimeFrom, dateTimeTo);
        return indiceOhlcvEntities.stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getTradeAssetOhlcvs(TradeAsset tradeAsset, OhlcvType ohlcvType, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        List<TradeAssetOhlcvEntity> tradeAssetOhlcvEntities = tradeAssetOhlcvRepository.findAllBySymbolAndOhlcvType(tradeAsset.getTradeId(), tradeAsset.getSymbol(), ohlcvType, dateTimeFrom, dateTimeTo);
        return tradeAssetOhlcvEntities.stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private void sendErrorAlarmIfEnabled(Trade trade, Throwable t) throws InterruptedException {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnError()) {
                String subject = String.format("[%s]", trade.getName());
                String content = ExceptionUtils.getRootCause(t).getMessage();
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

    private void buyTradeAsset(Trade trade, TradeAsset tradeAsset, OrderType orderType, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        OrderResult orderResult = null;
        String errorMessage = null;
        try {
            TradeClient tradeClient = TradeClientFactory.getClient(trade);
            tradeClient.buyAsset(tradeAsset, orderType, quantity, price);
            if (trade.isAlarmOnOrder()) {
                if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                    String subject = String.format("[%s]", trade.getName());
                    String content = String.format("[%s] Buy %s", tradeAsset.getName(), quantity);
                    alarmService.sendAlarm(trade.getAlarmId(), subject, content);
                }
            }
            orderResult = OrderResult.COMPLETED;
        } catch(Throwable e) {
            orderResult = OrderResult.FAILED;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            saveTradeOrder(OrderKind.BUY, tradeId, tradeAsset.getSymbol(), tradeAsset.getName(), orderType, quantity, price, orderResult, errorMessage);
        }
    }

    private void sellBalanceAsset(Trade trade, BalanceAsset balanceAsset, OrderType orderType, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        OrderResult orderResult = null;
        String errorMessage = null;
        try {
            TradeClient tradeClient = TradeClientFactory.getClient(trade);
            tradeClient.sellAsset(balanceAsset, orderType, quantity, price);
            if (trade.isAlarmOnOrder()) {
                if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                    String subject = String.format("[%s]", trade.getName());
                    String content = String.format("[%s] Sell %s", balanceAsset.getName(), quantity);
                    alarmService.sendAlarm(trade.getAlarmId(), subject, content);
                }
            }
            orderResult = OrderResult.COMPLETED;
        } catch(Throwable e) {
            orderResult = OrderResult.FAILED;
            errorMessage = e.getMessage();
            throw e;
        } finally {
            saveTradeOrder(OrderKind.SELL, tradeId, balanceAsset.getSymbol(), balanceAsset.getName(), orderType, quantity, price, orderResult, errorMessage);
        }
    }

    private void saveTradeOrder(OrderKind orderKind, String tradeId, String symbol, String assetName, OrderType orderType, BigDecimal quantity, BigDecimal price, OrderResult orderResult, String errorMessage) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
        transactionTemplate.executeWithoutResult(transactionStatus -> {
            orderRepository.saveAndFlush(OrderEntity.builder()
                    .orderId(IdGenerator.uuid())
                    .orderAt(LocalDateTime.now())
                    .orderKind(orderKind)
                    .tradeId(tradeId)
                    .symbol(symbol)
                    .assetName(assetName)
                    .orderType(orderType)
                    .quantity(quantity)
                    .price(price)
                    .orderResult(orderResult)
                    .errorMessage(errorMessage)
                    .build());
        });
    }

}
