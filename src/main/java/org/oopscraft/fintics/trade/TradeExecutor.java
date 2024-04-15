package org.oopscraft.fintics.trade;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.service.AssetService;
import org.oopscraft.fintics.service.IndiceService;
import org.oopscraft.fintics.service.OrderService;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class TradeExecutor {

    private final PlatformTransactionManager transactionManager;

    private final IndiceService indiceService;

    private final AssetService assetService;

    private final OrderService orderService;

    private final AlarmService alarmService;

    private Logger log = (Logger) LoggerFactory.getLogger(this.getClass());

    private final Map<String,BigDecimal> strategyResultMap = new HashMap<>();

    private final Map<String,Integer> strategyResultCountMap = new HashMap<>();

    @Builder
    private TradeExecutor(PlatformTransactionManager transactionManager, IndiceService indiceService, AssetService assetService, OrderService orderService, AlarmService alarmService) {
        this.transactionManager = transactionManager;
        this.indiceService = indiceService;
        this.assetService = assetService;
        this.orderService = orderService;
        this.alarmService = alarmService;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public void execute(Trade trade, Strategy strategy, LocalDateTime dateTime, IndiceClient indiceClient, BrokerClient tradeClient) throws InterruptedException {
        log.info("[{}] Check trade", trade.getTradeName());

        // check market opened
        if(!tradeClient.isOpened(dateTime)) {
            log.info("[{}] Market not opened.", trade.getTradeName());
            return;
        }

        // checks start,end time
        if (!isOperatingTime(trade, dateTime)) {
            log.info("[{}] Not operating time - {} ~ {}", trade.getTradeName(), trade.getStartAt(), trade.getEndAt());
            return;
        }

        // indice profiles
        List<Indice> indices = indiceService.getIndices();
        List<IndiceProfile> indiceProfiles = new ArrayList<>();
        for(Indice indice : indices) {
            // minute ohlcvs
            List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(indice.getIndiceId(), dateTime);
            List<Ohlcv> previousMinuteOhlcvs = getPreviousIndiceMinuteOhlcvs(indice.getIndiceId(), minuteOhlcvs, dateTime);
            minuteOhlcvs.addAll(previousMinuteOhlcvs);

            // daily ohlcvs
            List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(indice.getIndiceId(), dateTime);
            List<Ohlcv> previousDailyOhlcvs = getPreviousIndiceDailyOhlcvs(indice.getIndiceId(), dailyOhlcvs, dateTime);
            dailyOhlcvs.addAll(previousDailyOhlcvs);

            // indice profile
            IndiceProfile indiceProfile = IndiceProfile.builder()
                    .target(indice)
                    .minuteOhlcvs(minuteOhlcvs)
                    .dailyOhlcvs(dailyOhlcvs)
                    .build();
            indiceProfiles.add(indiceProfile);
        }

        // balance
        Balance balance = tradeClient.getBalance();

        // checks buy condition
        for (TradeAsset tradeAsset : trade.getTradeAssets()) {
            try {
                Thread.sleep(100);

                // check enabled
                if (!tradeAsset.isEnabled()) {
                    continue;
                }

                // logging
                log.info("[{}] Check asset", tradeAsset.getAssetName());

                // minute ohlcvs
                List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset, dateTime);
                List<Ohlcv> previousMinuteOhlcvs = getPreviousAssetMinuteOhlcvs(tradeAsset.getAssetId(), minuteOhlcvs, dateTime);
                minuteOhlcvs.addAll(previousMinuteOhlcvs);

                // daily ohlcvs
                List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset, dateTime);
                List<Ohlcv> previousDailyOhlcvs = getPreviousAssetDailyOhlcvs(tradeAsset.getAssetId(), dailyOhlcvs, dateTime);
                dailyOhlcvs.addAll(previousDailyOhlcvs);

                // asset profile
                AssetProfile assetProfile = AssetProfile.builder()
                        .target(tradeAsset)
                        .minuteOhlcvs(minuteOhlcvs)
                        .dailyOhlcvs(dailyOhlcvs)
                        .build();

                // logging
                log.info("[{}] MinuteOhlcvs({}):{}", tradeAsset.getAssetName(), assetProfile.getMinuteOhlcvs().size(), assetProfile.getMinuteOhlcvs().isEmpty() ? null : assetProfile.getMinuteOhlcvs().get(0));
                log.info("[{}] DailyOhlcvs({}):{}", tradeAsset.getAssetName(), assetProfile.getDailyOhlcvs().size(), assetProfile.getDailyOhlcvs().isEmpty() ? null : assetProfile.getDailyOhlcvs().get(0));

                // order book
                OrderBook orderBook = tradeClient.getOrderBook(tradeAsset);

                // executes trade asset decider
                StrategyExecutor strategyExecutor = StrategyExecutor.builder()
                        .indiceProfiles(indiceProfiles)
                        .assetProfile(assetProfile)
                        .strategy(strategy)
                        .variables(trade.getStrategyVariables())
                        .dateTime(dateTime)
                        .orderBook(orderBook)
                        .balance(balance)
                        .build();
                strategyExecutor.setLog(log);
                Instant startTime = Instant.now();
                BigDecimal strategyResult = strategyExecutor.execute();
                log.info("[{}] Rule script execution elapsed:{}", tradeAsset.getAssetName(), Duration.between(startTime, Instant.now()));
                log.info("[{}] ruleScriptResult: {}", tradeAsset.getAssetName(), strategyResult);

                // check rule script result and count
                BigDecimal previousRuleScriptResult = strategyResultMap.get(tradeAsset.getAssetId());
                int strategyResultCount = strategyResultCountMap.getOrDefault(tradeAsset.getAssetId(), 0);
                if (Objects.equals(strategyResult, previousRuleScriptResult)) {
                    strategyResultCount++;
                } else {
                    strategyResultCount = 1;
                }
                strategyResultMap.put(tradeAsset.getAssetId(), strategyResult);
                strategyResultCountMap.put(tradeAsset.getAssetId(), strategyResultCount);

                // checks threshold exceeded
                log.info("[{}] strategyResultCount: {}", tradeAsset.getAssetName(), strategyResultCount);
                if (strategyResultCount < trade.getThreshold()) {
                    log.info("[{}] Threshold has not been exceeded yet - threshold is {}", tradeAsset.getAssetName(), trade.getThreshold());
                    continue;
                }

                // null is no operation
                if (strategyResult == null) {
                    continue;
                }

                // calculate exceeded amount
                BigDecimal totalAmount = balance.getTotalAmount();
                BigDecimal holdRatio = tradeAsset.getHoldRatio();
                BigDecimal holdRatioAmount = totalAmount
                        .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                        .multiply(holdRatio)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal holdConditionResultAmount = holdRatioAmount
                        .multiply(strategyResult)
                        .setScale(2, RoundingMode.HALF_UP);
                BigDecimal balanceAssetAmount = balance.getBalanceAsset(tradeAsset.getAssetId())
                        .map(BalanceAsset::getValuationAmount)
                        .orElse(BigDecimal.ZERO);
                BigDecimal exceededAmount = holdConditionResultAmount.subtract(balanceAssetAmount);

                // check change is over 10%(9%)
                BigDecimal thresholdAmount = holdRatioAmount.multiply(BigDecimal.valueOf(0.09));
                if(exceededAmount.abs().compareTo(thresholdAmount) < 0) {
                    continue;
                }

                // buy (exceedAmount is over zero)
                if (exceededAmount.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal price = orderBook.getAskPrice();
                    BigDecimal priceTick = tradeClient.getPriceTick(tradeAsset, price);
                    if(priceTick != null) {
                        price = price.min(orderBook.getBidPrice().add(priceTick));
                    }
                    BigDecimal quantity = exceededAmount.divide(price, MathContext.DECIMAL32);
                    buyTradeAsset(tradeClient, trade, tradeAsset, quantity, price);
                }

                // sell (exceedAmount is under zero)
                if (exceededAmount.compareTo(BigDecimal.ZERO) < 0) {
                    BigDecimal price = orderBook.getBidPrice();
                    BigDecimal priceTick = tradeClient.getPriceTick(tradeAsset, price);
                    if(priceTick != null) {
                        price = price.max(orderBook.getAskPrice().subtract(priceTick));
                    }
                    BigDecimal quantity = exceededAmount.abs().divide(price, MathContext.DECIMAL32);
                    // if holdConditionResult is zero, sell quantity is all
                    if (strategyResult.compareTo(BigDecimal.ZERO) == 0) {
                        BalanceAsset balanceAsset = balance.getBalanceAsset(tradeAsset.getAssetId()).orElse(null);
                        if (balanceAsset != null) {
                            quantity = balanceAsset.getOrderableQuantity();
                        }
                    }
                    sellTradeAsset(tradeClient, trade, tradeAsset, quantity, price);
                }

            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                sendErrorAlarmIfEnabled(trade, tradeAsset, e);
            }
        }
    }

    private boolean isOperatingTime(Trade trade, LocalDateTime dateTime) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return false;
        }
        LocalTime time = dateTime.toLocalTime();
        return time.isAfter(trade.getStartAt()) && time.isBefore(trade.getEndAt());
    }

    private List<Ohlcv> getPreviousIndiceMinuteOhlcvs(Indice.Id indiceId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusMinutes(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return indiceService.getIndiceOhlcvs(indiceId, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, PageRequest.of(0, 1000));
    }

    private List<Ohlcv> getPreviousIndiceDailyOhlcvs(Indice.Id indiceId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusDays(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return indiceService.getIndiceOhlcvs(indiceId, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo, PageRequest.of(0, 360));
    }

    private List<Ohlcv> getPreviousAssetMinuteOhlcvs(String assetId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusWeeks(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusMinutes(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return assetService.getAssetOhlcvs(assetId, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, PageRequest.of(0, 1000));
    }

    private List<Ohlcv> getPreviousAssetDailyOhlcvs(String assetId, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime dateTimeFrom = dateTime.minusYears(1);
        LocalDateTime dateTimeTo = ohlcvs.isEmpty() ? dateTime : ohlcvs.get(ohlcvs.size()-1).getDateTime().minusDays(1);
        if(dateTimeTo.isBefore(dateTimeFrom)) {
            return new ArrayList<>();
        }
        return assetService.getAssetOhlcvs(assetId, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo, PageRequest.of(0, 360));
    }

    private void sendErrorAlarmIfEnabled(Trade trade, TradeAsset tradeAsset, Throwable t) {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnError()) {
                String subject = String.format("[%s - %s]", trade.getTradeName(), tradeAsset != null ? tradeAsset.getAssetName() : "");
                String content = ExceptionUtils.getRootCause(t).getMessage();
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

    private void buyTradeAsset(BrokerClient brokerClient, Trade trade, TradeAsset tradeAsset, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        Order order = Order.builder()
                .orderAt(LocalDateTime.now())
                .type(Order.Type.BUY)
                .kind(trade.getOrderKind())
                .tradeId(tradeAsset.getTradeId())
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
                .quantity(quantity)
                .price(price)
                .build();
        log.info("[{}] buyTradeAsset: {}", tradeAsset.getAssetName(), order);
        try {
            // check waiting order exists
            Order waitingOrder = brokerClient.getWaitingOrders().stream()
                    .filter(element ->
                            Objects.equals(element.getSymbol(), order.getSymbol())
                                    && element.getType() == order.getType())
                    .findFirst()
                    .orElse(null);
            if (waitingOrder != null) {
                // if limit type order, amend order
                if (waitingOrder.getKind() == Order.Kind.LIMIT) {
                    waitingOrder.setPrice(price);
                    log.info("[{}] amend buy order:{}", tradeAsset.getAssetName(), waitingOrder);
                    brokerClient.amendOrder(waitingOrder);
                }
                return;
            }

            // submit buy order
            brokerClient.submitOrder(order);
            order.setResult(Order.Result.COMPLETED);

            // alarm
            sendOrderAlarmIfEnabled(trade, order);

        } catch (Throwable e) {
            order.setResult(Order.Result.FAILED);
            order.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            saveTradeOrder(order);
        }
    }

    private void sellTradeAsset(BrokerClient brokerClient, Trade trade, TradeAsset tradeAsset, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        Order order = Order.builder()
                .orderAt(LocalDateTime.now())
                .type(Order.Type.SELL)
                .kind(trade.getOrderKind())
                .tradeId(tradeAsset.getTradeId())
                .assetId(tradeAsset.getAssetId())
                .assetName(tradeAsset.getAssetName())
                .quantity(quantity)
                .price(price)
                .build();
        log.info("[{}] sellTradeAsset: {}", tradeAsset.getAssetName(), order);
        try {
            // check waiting order exists
            Order waitingOrder = brokerClient.getWaitingOrders().stream()
                    .filter(element ->
                            Objects.equals(element.getSymbol(), order.getSymbol())
                                    && element.getType() == order.getType())
                    .findFirst()
                    .orElse(null);
            if (waitingOrder != null) {
                // if limit type order, amend order
                if (waitingOrder.getKind() == Order.Kind.LIMIT) {
                    waitingOrder.setPrice(price);
                    log.info("[{}] amend sell order:{}", tradeAsset.getAssetName(), waitingOrder);
                    brokerClient.amendOrder(waitingOrder);
                }
                return;
            }

            // submit sell order
            brokerClient.submitOrder(order);
            order.setResult(Order.Result.COMPLETED);

            // alarm
            sendOrderAlarmIfEnabled(trade, order);

        } catch (Throwable e) {
            order.setResult(Order.Result.FAILED);
            order.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            saveTradeOrder(order);
        }
    }

    private void saveTradeOrder(Order order) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
        transactionTemplate.executeWithoutResult(transactionStatus ->
                orderService.saveOrder(order));
    }

    private void sendOrderAlarmIfEnabled(Trade trade, Order order) {
        if (trade.isAlarmOnOrder()) {
            if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                String subject = String.format("[%s]", trade.getTradeName());
                String content = String.format("[%s] %s(%s) - price: %s / quantity: %s",
                        order.getAssetName(),
                        order.getType(),
                        order.getKind(),
                        order.getPrice(),
                        order.getQuantity());
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

}
