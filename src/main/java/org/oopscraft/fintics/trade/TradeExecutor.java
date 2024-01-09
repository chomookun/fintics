package org.oopscraft.fintics.trade;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.fintics.client.indice.IndiceClient;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientFactory;
import org.oopscraft.fintics.dao.AssetOhlcvRepository;
import org.oopscraft.fintics.dao.IndiceOhlcvRepository;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.trade.order.OrderOperator;
import org.oopscraft.fintics.trade.order.OrderOperatorContext;
import org.oopscraft.fintics.trade.order.OrderOperatorFactory;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TradeExecutor {

    private final ApplicationContext applicationContext;

    private final Trade trade;

    private final IndiceClient indiceClient;

    private final TradeClient tradeClient;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final AlarmService alarmService;

    private final Logger log;

    private final Map<String,Boolean> holdConditionResultMap = new HashMap<>();

    private final Map<String,Integer> holdConditionResultCountMap = new HashMap<>();

    @Builder
    public TradeExecutor(Trade trade, IndiceClient indiceClient, TradeClient tradeClient, ApplicationContext applicationContext, Logger log) {
        this.applicationContext = applicationContext;
        this.trade = trade;
        this.indiceClient = indiceClient;
        this.tradeClient = tradeClient;
        this.indiceOhlcvRepository = applicationContext.getBean(IndiceOhlcvRepository.class);
        this.assetOhlcvRepository = applicationContext.getBean(AssetOhlcvRepository.class);
        this.alarmService = applicationContext.getBean(AlarmService.class);
        this.log = log;
    }

    public void execute(LocalDateTime dateTime) throws InterruptedException {

        // check market opened
        if(!tradeClient.isOpened(dateTime)) {
            log.info("Market not opened.");
            return;
        }

        // checks start,end time
        if (!isOperatingTime(dateTime.toLocalTime())) {
            log.info("Not operating time - [{}] {} ~ {}", trade.getName(), trade.getStartAt(), trade.getEndAt());
            return;
        }

        // indice indicators
        List<IndiceIndicator> indiceIndicators = new ArrayList<>();
        for(IndiceSymbol symbol : IndiceSymbol.values()) {
            indiceClient.getMinuteOhlcvs(symbol, dateTime);

            // minute ohlcvs
            List<Ohlcv> minuteOhlcvs = indiceClient.getMinuteOhlcvs(symbol, dateTime);
            List<Ohlcv> previousMinuteOhlcvs = getPreviousIndiceMinuteOhlcvs(symbol, minuteOhlcvs, dateTime);
            minuteOhlcvs.addAll(previousMinuteOhlcvs);

            // daily ohlcvs
            List<Ohlcv> dailyOhlcvs = indiceClient.getDailyOhlcvs(symbol, dateTime);
            List<Ohlcv> previousDailyOhlcvs = getPreviousIndiceDailyOhlcvs(symbol, dailyOhlcvs, dateTime);
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
            try {
                Thread.sleep(100);

                // check enabled
                if (!tradeAsset.isEnabled()) {
                    continue;
                }

                // logging
                log.info("Check asset - [{}]", tradeAsset.getName());

                // indicator
                List<Ohlcv> minuteOhlcvs = tradeClient.getMinuteOhlcvs(tradeAsset, dateTime);
                List<Ohlcv> previousMinuteOhlcvs = getPreviousAssetMinuteOhlcvs(trade.getClientId(), tradeAsset.getSymbol(), minuteOhlcvs, dateTime);
                minuteOhlcvs.addAll(previousMinuteOhlcvs);

                List<Ohlcv> dailyOhlcvs = tradeClient.getDailyOhlcvs(tradeAsset, dateTime);
                List<Ohlcv> previousDailyOhlcvs = getPreviousAssetDailyOhlcvs(trade.getClientId(), tradeAsset.getSymbol(), dailyOhlcvs, dateTime);
                dailyOhlcvs.addAll(previousDailyOhlcvs);

                AssetIndicator assetIndicator = AssetIndicator.builder()
                        .symbol(tradeAsset.getSymbol())
                        .name(tradeAsset.getName())
                        .minuteOhlcvs(minuteOhlcvs)
                        .dailyOhlcvs(dailyOhlcvs)
                        .build();

                // order book
                OrderBook orderBook = tradeClient.getOrderBook(tradeAsset, dateTime);

                // executes trade asset decider
                TradeAssetDecider tradeAssetDecider = TradeAssetDecider.builder()
                        .holdCondition(trade.getHoldCondition())
                        .logger(log)
                        .dateTime(dateTime)
                        .orderBook(orderBook)
                        .balance(balance)
                        .indiceIndicators(indiceIndicators)
                        .assetIndicator(assetIndicator)
                        .build();
                Boolean holdConditionResult = tradeAssetDecider.execute();
                log.info("holdConditionResult: {}", holdConditionResult);

                // order operator
                OrderOperatorContext orderOperatorContext = OrderOperatorContext.builder()
                        .operatorId("SIMPLE")
                        .tradeClient(tradeClient)
                        .trade(trade)
                        .orderBook(orderBook)
                        .balance(balance)
                        .build();
                OrderOperator orderOperator = OrderOperatorFactory.getOrderOperator(orderOperatorContext);

                // 0. checks threshold exceeded
                int consecutiveCountOfHoldConditionResult = getConsecutiveCountOfHoldConditionResult(tradeAsset.getSymbol(), holdConditionResult);
                log.info("consecutiveCountOfHoldConditionResult: {}", consecutiveCountOfHoldConditionResult);
                if (consecutiveCountOfHoldConditionResult < trade.getThreshold()) {
                    log.info("Threshold has not been exceeded yet - threshold is {}", trade.getThreshold());
                    continue;
                }

                // 1. null is no operation
                if (holdConditionResult == null) {
                    continue;
                }

                // 2. buy asset
                if (holdConditionResult.equals(Boolean.TRUE)) {
                    orderOperator.buyTradeAsset(tradeAsset);
                }

                // 3. sell asset
                else if (holdConditionResult.equals(Boolean.FALSE)) {
                    orderOperator.sellTradeAsset(tradeAsset);
                }
            } catch (Throwable e) {
                log.error(e.getMessage(), e);
                sendErrorAlarmIfEnabled(tradeAsset, e);
            }
        }
    }

    private boolean isOperatingTime(LocalTime time) {
        if(trade.getStartAt() == null || trade.getEndAt() == null) {
            return false;
        }
        return time.isAfter(trade.getStartAt()) && time.isBefore(trade.getEndAt());
    }

    private List<Ohlcv> getPreviousIndiceMinuteOhlcvs(IndiceSymbol symbol, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime lastDateTime = !ohlcvs.isEmpty()
                ? ohlcvs.get(ohlcvs.size()-1).getDateTime()
                : dateTime;
        return indiceOhlcvRepository.findAllBySymbolAndOhlcvType(symbol,
                        OhlcvType.MINUTE,
                        lastDateTime.minusWeeks(1),
                        lastDateTime.minusMinutes(1),
                        PageRequest.of(0, 360)
                ).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousIndiceDailyOhlcvs(IndiceSymbol symbol, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime lastDateTime = !ohlcvs.isEmpty()
                ? ohlcvs.get(ohlcvs.size()-1).getDateTime()
                : dateTime;
        return indiceOhlcvRepository.findAllBySymbolAndOhlcvType(
                        symbol,
                        OhlcvType.MINUTE,
                        lastDateTime.minusYears(1),
                        lastDateTime.minusDays(1),
                        PageRequest.of(0, 360)
                )
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousAssetMinuteOhlcvs(String clientId, String symbol, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime lastDateTime = !ohlcvs.isEmpty()
                ? ohlcvs.get(ohlcvs.size()-1).getDateTime()
                : dateTime;
        return assetOhlcvRepository.findAllBySymbolAndOhlcvType(
                        clientId,
                        symbol,
                        OhlcvType.MINUTE,
                        lastDateTime.minusWeeks(1),
                        lastDateTime.minusMinutes(1),
                        PageRequest.of(0, 1000))
                .stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private List<Ohlcv> getPreviousAssetDailyOhlcvs(String clientId, String symbol, List<Ohlcv> ohlcvs, LocalDateTime dateTime) {
        LocalDateTime lastDateTime = !ohlcvs.isEmpty()
                ? ohlcvs.get(ohlcvs.size()-1).getDateTime()
                : dateTime;
        return assetOhlcvRepository.findAllBySymbolAndOhlcvType(
                        clientId,
                        symbol,
                        OhlcvType.MINUTE,
                        lastDateTime.minusYears(1),
                        lastDateTime.minusDays(1),
                        PageRequest.of(0, 360)
                ).stream()
                .map(Ohlcv::from)
                .collect(Collectors.toList());
    }

    private int getConsecutiveCountOfHoldConditionResult(String symbol, Boolean holdConditionResult) {
        Boolean previousHoldConditionResult = holdConditionResultMap.get(symbol);
        int holdConditionResultCount = holdConditionResultCountMap.getOrDefault(symbol, 0);

        // increases match count
        if(Objects.equals(holdConditionResult, previousHoldConditionResult)) {
            holdConditionResultCount ++;
        }else{
            holdConditionResultCount = 1;
        }

        // store
        holdConditionResultMap.put(symbol, holdConditionResult);
        holdConditionResultCountMap.put(symbol, holdConditionResultCount);

        // return
        return holdConditionResultCount;
    }

    private void sendErrorAlarmIfEnabled(TradeAsset tradeAsset, Throwable t) throws InterruptedException {
        if(trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
            if (trade.isAlarmOnError()) {
                String subject = String.format("[%s - %s]", trade.getName(), tradeAsset != null ? tradeAsset.getName() : "");
                String content = ExceptionUtils.getRootCause(t).getMessage();
                alarmService.sendAlarm(trade.getAlarmId(), subject, content);
            }
        }
    }

}
