package org.chomookun.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.execution.model.Execution;
import org.chomookun.fintics.dao.BasketEntity;
import org.chomookun.fintics.dao.BasketRepository;
import org.chomookun.fintics.dao.OhlcvRepository;
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

    private static final String SCHEDULER_ID = "OhlcvUnusedDeleter";

    private final OhlcvRepository ohlcvRepository;

    private final BasketRepository basketRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 10_000, fixedDelay = 1_000 * 60 * 10)
    public void delete() {
        log.info("OhlcvPastCollector - Start to delete unused ohlcv.");
        Execution execution = startExecution(SCHEDULER_ID);
        try {
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

            // updates execution
            execution.getTotalCount().incrementAndGet();
            updateExecution(execution);

            // delete not used asset ohlcv
            for (String unusedOhlcvAssetId : unusedOhlcvAssetIds) {
                try {
                    runWithTransaction(transactionManager, () ->
                            ohlcvRepository.deleteByAssetId(unusedOhlcvAssetId));
                    execution.getSuccessCount().incrementAndGet();
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                    execution.getFailCount().incrementAndGet();
                } finally {
                    updateExecution(execution);
                }
            }

            // success execution
            successExecution(execution);

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            failExecution(execution, e);
            sendSystemAlarm(execution);
            throw new RuntimeException(e);
        }
        log.info("OhlcvUnusedDeleter - End to delete unused ohlcv.");
    }

}
