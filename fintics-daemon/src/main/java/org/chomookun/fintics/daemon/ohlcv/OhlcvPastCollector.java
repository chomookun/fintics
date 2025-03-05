package org.chomookun.fintics.daemon.ohlcv;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.execution.model.Execution;
import org.chomookun.fintics.core.FinticsCoreProperties;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.basket.model.BasketSearch;
import org.chomookun.fintics.core.ohlcv.client.OhlcvClient;
import org.chomookun.fintics.core.ohlcv.entity.OhlcvEntity;
import org.chomookun.fintics.core.ohlcv.repository.OhlcvRepository;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.chomookun.fintics.daemon.common.AbstractTask;
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
public class OhlcvPastCollector extends AbstractTask {

    private final static String SCHEDULER_ID = "OhlcvPastCollector";

    private final FinticsCoreProperties finticsCoreProperties;

    @PersistenceContext
    private final EntityManager entityManager;

    private final PlatformTransactionManager transactionManager;

    private final BasketService basketService;

    private final OhlcvRepository assetOhlcvRepository;

    private final OhlcvClient ohlcvClient;

    /**
     * schedule collect
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 1_000 * 60 * 10)
    public void collect() {
        log.info("OhlcvPastCollector - Start collect past ohlcv.");
        Execution execution = startExecution(SCHEDULER_ID);
        try {
            // expired date time
            LocalDateTime expiredDateTime = LocalDateTime.now()
                    .minusMonths(finticsCoreProperties.getDataRetentionMonths());
            // past ohlcv is based on basket (using ohlcv client)
            List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();
            for (Basket basket : baskets) {
                List<BasketAsset> basketAssets = basket.getBasketAssets();
                for (BasketAsset basketAsset : basketAssets) {
                    execution.getTotalCount().incrementAndGet();
                    try {
                        if (ohlcvClient.isSupported(basketAsset)) {
                            collectPastDailyOhlcvs(basketAsset, expiredDateTime);
                            collectPastMinuteOhlcvs(basketAsset, expiredDateTime);
                        }
                        execution.getSuccessCount().incrementAndGet();
                    } catch (Throwable e) {
                        log.warn(e.getMessage());
                        execution.getFailCount().incrementAndGet();
                    } finally {
                        updateExecution(execution);
                    }
                }
            }

            // success execution
            successExecution(execution);

        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            failExecution(execution, e);
            sendSystemAlarm(execution);
            throw new RuntimeException(e);
        }
        log.info("OhlcvPastCollector - End collect past ohlcv");
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
        List<Ohlcv> ohlcvs = ohlcvClient.getOhlcvs(asset, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo);
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
        List<Ohlcv> ohlcvs = ohlcvClient.getOhlcvs(asset, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo);
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
