package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.FinticsProperties;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.dao.BrokerAssetOhlcvEntity;
import org.oopscraft.fintics.dao.BrokerAssetOhlcvRepository;
import org.oopscraft.fintics.dao.TradeEntity;
import org.oopscraft.fintics.dao.TradeRepository;
import org.oopscraft.fintics.model.Ohlcv;
import org.oopscraft.fintics.model.Trade;
import org.oopscraft.fintics.model.TradeAsset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BrokerAssetOhlcvCollector {

    private final FinticsProperties finticsProperties;

    private final TradeRepository tradeRepository;

    private final BrokerClientFactory brokerClientFactory;

    private final BrokerAssetOhlcvRepository tradeAssetOhlcvRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    @Scheduled(initialDelay = 1_000, fixedDelay = 60_000)
    @Transactional
    public void collect() {
        log.info("Start collect broker asset ohlcv.");
        List<TradeEntity> tradeEntities = tradeRepository.findAll();
        for (TradeEntity tradeEntity : tradeEntities) {
            try {
                Trade trade = Trade.from(tradeEntity);
                for (TradeAsset tradeAsset : trade.getTradeAssets()) {
                    saveAssetOhlcv(trade, tradeAsset);
                    deletePastRetentionOhlcv(trade, tradeAsset);
                }
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
        log.info("End collect broker asset ohlcv");
    }

    private void saveAssetOhlcv(Trade trade, TradeAsset tradeAsset) throws InterruptedException {
        BrokerClient brokerClient = brokerClientFactory.getObject(trade);
        LocalDateTime dateTime = LocalDateTime.now();

        // minutes
        List<Ohlcv> minuteOhlcvs = brokerClient.getMinuteOhlcvs(tradeAsset, dateTime);
        Collections.reverse(minuteOhlcvs);
        LocalDateTime minuteLastDateTime = tradeAssetOhlcvRepository.findMaxDateTimeByBrokerIdAndAssetIdAndType(trade.getBrokerId(), tradeAsset.getAssetId(), Ohlcv.Type.MINUTE)
                .orElse(getExpiredDateTime())
                .minusMinutes(2);
        List<BrokerAssetOhlcvEntity> minuteTradeAssetOhlcvEntities = minuteOhlcvs.stream()
                .filter(ohlcv -> ohlcv.getDateTime().isAfter(minuteLastDateTime))
                .limit(10)
                .map(ohlcv -> toAssetOhlcvEntity(trade.getBrokerId(), tradeAsset.getAssetId(), ohlcv))
                .collect(Collectors.toList());
        tradeAssetOhlcvRepository.saveAllAndFlush(minuteTradeAssetOhlcvEntities);

        // daily
        List<Ohlcv> dailyOhlcvs = brokerClient.getDailyOhlcvs(tradeAsset, dateTime);
        Collections.reverse(dailyOhlcvs);
        LocalDateTime dailyLastDateTime = tradeAssetOhlcvRepository.findMaxDateTimeByBrokerIdAndAssetIdAndType(trade.getBrokerId(), tradeAsset.getAssetId(), Ohlcv.Type.DAILY)
                .orElse(getExpiredDateTime())
                .minusDays(2);
        List<BrokerAssetOhlcvEntity> dailyOhlcvEntities = dailyOhlcvs.stream()
                .filter(ohlcv -> ohlcv.getDateTime().isAfter(dailyLastDateTime))
                .limit(10)
                .map(ohlcv -> toAssetOhlcvEntity(trade.getBrokerId(), tradeAsset.getAssetId(), ohlcv))
                .collect(Collectors.toList());
        tradeAssetOhlcvRepository.saveAllAndFlush(dailyOhlcvEntities);
    }

    private BrokerAssetOhlcvEntity toAssetOhlcvEntity(String tradeClientId, String assetId, Ohlcv ohlcv) {
        return BrokerAssetOhlcvEntity.builder()
                .brokerId(tradeClientId)
                .assetId(assetId)
                .dateTime(ohlcv.getDateTime())
                .type(ohlcv.getType())
                .openPrice(ohlcv.getOpenPrice())
                .highPrice(ohlcv.getHighPrice())
                .lowPrice(ohlcv.getLowPrice())
                .closePrice(ohlcv.getClosePrice())
                .volume(ohlcv.getVolume())
                .build();
    }

    private LocalDateTime getExpiredDateTime() {
        return LocalDateTime.now().minusMonths(finticsProperties.getOhlcvRetentionMonths());
    }

    private void deletePastRetentionOhlcv(Trade trade, TradeAsset tradeAsset) {
        entityManager.createQuery(
                        "delete" +
                                " from BrokerAssetOhlcvEntity" +
                                " where brokerId = :brokerId " +
                                " and assetId = :symbol " +
                                " and dateTime < :expiredDateTime")
                .setParameter("brokerId", trade.getBrokerId())
                .setParameter("symbol", tradeAsset.getAssetId())
                .setParameter("expiredDateTime", getExpiredDateTime())
                .executeUpdate();
        entityManager.flush();
        entityManager.clear();
    }

}
