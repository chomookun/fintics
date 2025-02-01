package org.chomookun.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.FinticsProperties;
import org.chomookun.fintics.client.ohlcv.OhlcvClient;
import org.chomookun.fintics.dao.OhlcvEntity;
import org.chomookun.fintics.dao.OhlcvRepository;
import org.chomookun.fintics.model.*;
import org.chomookun.fintics.service.BasketService;
import org.chomookun.fintics.service.TradeService;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class OhlcvPastCollector extends AbstractScheduler {

    private final FinticsProperties finticsProperties;

    @PersistenceContext
    private final EntityManager entityManager;

    private final PlatformTransactionManager transactionManager;

    private final TradeService tradeService;

    private final BasketService basketService;

    private final OhlcvRepository assetOhlcvRepository;

    private final OhlcvClient ohlcvClient;

    /**
     * schedule collect
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 600_000)
    public void collect() {
        try {
            log.info("OhlcvPastCollector - Start collect past ohlcv.");
            // expired date time
            LocalDateTime expiredDateTime = LocalDateTime.now()
                    .minusMonths(finticsProperties.getDataRetentionMonths());
            // past ohlcv is based on basket (using ohlcv client)
            List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();
            for (Basket basket : baskets) {
                List<BasketAsset> basketAssets = basket.getBasketAssets();
                for (BasketAsset basketAsset : basketAssets) {
                    try {
                        if (ohlcvClient.isSupported(basketAsset)) {
                            collectPastDailyOhlcvs(basketAsset, expiredDateTime);
                            collectPastMinuteOhlcvs(basketAsset, expiredDateTime);
                        }
                    } catch (Throwable e) {
                        log.warn(e.getMessage());
                        sendSystemAlarm(this.getClass(), String.format("[%s] %s - %s", basket.getName(), basketAsset.getName(), e.getMessage()));
                    }
                }
            }
            log.info("OhlcvPastCollector - End collect past ohlcv");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * collect past daily ohlcv
     * @param asset asset
     * @param expiredDateTime expired date time
     */
    void collectPastDailyOhlcvs(Asset asset, LocalDateTime expiredDateTime) {
        // date time
        LocalDateTime dateTimeTo = getMinDatetime(asset.getAssetId(), Ohlcv.Type.DAILY)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusYears(1);
        // check expired date time
        if(dateTimeFrom.isBefore(expiredDateTime)) {
            dateTimeFrom = expiredDateTime;
        }
        // get daily ohlcvs
        List<Ohlcv> ohlcvs = ohlcvClient.getOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo, Pageable.unpaged());
        // convert and save
        List<OhlcvEntity> dailyOhlcvEntities = ohlcvs.stream()
                .map(ohlcv -> OhlcvEntity.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(ohlcv.getDateTime())
                        .timeZone(ohlcv.getTimeZone())
                        .type(ohlcv.getType())
                        .open(ohlcv.getOpen())
                        .high(ohlcv.getHigh())
                        .low(ohlcv.getLow())
                        .close(ohlcv.getClose())
                        .volume(ohlcv.getVolume())
                        .interpolated(ohlcv.isInterpolated())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastAssetDailyOhlcvEntities[%s]", asset.getName());
        log.debug("OhlcvPastCollector - save {}:{}", unitName, dailyOhlcvEntities.size());
        saveEntities(unitName, dailyOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    /**
     * collect past minute ohlcvs
     * @param asset asset
     * @param expiredDateTime expired datetime
     */
    void collectPastMinuteOhlcvs(Asset asset, LocalDateTime expiredDateTime) {
        LocalDateTime dateTimeTo = getMinDatetime(asset.getAssetId(), Ohlcv.Type.MINUTE)
                .orElse(LocalDateTime.now());
        LocalDateTime dateTimeFrom = dateTimeTo.minusMonths(1);
        // check expired date time
        if(dateTimeFrom.isBefore(expiredDateTime)) {
            dateTimeFrom = expiredDateTime;
        }
        // check daily min date time (in case of new IPO security)
        LocalDateTime dailyMinDatetime = getMinDatetime(asset.getAssetId(), Ohlcv.Type.DAILY).orElse(null);
        if (dailyMinDatetime != null) {
            if (dateTimeFrom.isBefore(dailyMinDatetime)) {
                dateTimeFrom = dailyMinDatetime;
            }
        }
        // get minute ohlcvs
        List<Ohlcv> ohlcvs = ohlcvClient.getOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, Pageable.unpaged());
        // convert and save
        List<OhlcvEntity> minuteOhlcvEntities = ohlcvs.stream()
                .map(ohlcv -> OhlcvEntity.builder()
                        .assetId(asset.getAssetId())
                        .dateTime(ohlcv.getDateTime())
                        .timeZone(ohlcv.getTimeZone())
                        .type(ohlcv.getType())
                        .open(ohlcv.getOpen())
                        .high(ohlcv.getHigh())
                        .low(ohlcv.getLow())
                        .close(ohlcv.getClose())
                        .volume(ohlcv.getVolume())
                        .interpolated(ohlcv.isInterpolated())
                        .build())
                .collect(Collectors.toList());
        String unitName = String.format("pastMinuteOhlcvEntities[%s]", asset.getName());
        log.debug("PastOhlcvCollector - save {}:{}", unitName, minuteOhlcvEntities.size());
        saveEntities(unitName, minuteOhlcvEntities, transactionManager, assetOhlcvRepository);
    }

    /**
     * get minimum date time
     * @param assetId asset id
     * @param type ohlcv type
     * @return return minimum datetime
     */
    Optional<LocalDateTime> getMinDatetime(String assetId, Ohlcv.Type type) {
        LocalDateTime minDatetime = entityManager.createQuery("select " +
                                " min(a.dateTime) " +
                                " from OhlcvEntity a " +
                                " where a.assetId = :assetId " +
                                " and a.type = :type",
                        LocalDateTime.class)
                .setParameter("assetId", assetId)
                .setParameter("type", type)
                .getSingleResult();
        return Optional.ofNullable(minDatetime);
    }

}
