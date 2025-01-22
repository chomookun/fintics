package org.chomoo.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomoo.fintics.dao.BasketEntity;
import org.chomoo.fintics.dao.BasketRepository;
import org.chomoo.fintics.dao.OhlcvRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OhlcvUnusedDeleter extends AbstractScheduler {

    private final OhlcvRepository ohlcvRepository;

    private final BasketRepository basketRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 10_000, fixedDelay = 600_000)
    public void delete() {
        try {
            log.info("OhlcvPastCollector - Start to delete unused ohlcv.");
            // gets distinct assetIds
            List<String> ohlcvAssetIds = ohlcvRepository.findDistinctAssetIds();

            // checks used in basket
            Set<String> basketAssetIds = new HashSet<>();
            List<BasketEntity> basketEntities = basketRepository.findAll();
            basketEntities.forEach(basketEntity -> {
                basketEntity.getBasketAssets().forEach(basketAssetEntity -> {
                    basketAssetIds.add(basketAssetEntity.getAssetId());
                });
            });

            // find ohlcv asset id not using in basket
            List<String> unusedOhlcvAssetIds = ohlcvAssetIds.stream()
                    .filter(ohlcvAssetId -> !basketAssetIds.contains(ohlcvAssetId))
                    .toList();

            // delete not used asset ohlcv
            for (String unusedOhlcvAssetId : unusedOhlcvAssetIds) {
                try {
                    runWithTransaction(transactionManager, () ->
                            ohlcvRepository.deleteByAssetId(unusedOhlcvAssetId));
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                    sendSystemAlarm(this.getClass(), String.format("[%s] %s", unusedOhlcvAssetId, e.getMessage()));
                }
            }
            log.info("OhlcvUnusedDeleter - End to delete unused ohlcv.");
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

}
