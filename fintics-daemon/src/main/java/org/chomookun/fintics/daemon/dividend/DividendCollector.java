package org.chomookun.fintics.daemon.dividend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.execution.model.Execution;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketSearch;
import org.chomookun.fintics.core.dividend.client.DividendClient;
import org.chomookun.fintics.core.dividend.entity.DividendEntity;
import org.chomookun.fintics.core.dividend.model.Dividend;
import org.chomookun.fintics.core.dividend.repository.DividendRepository;
import org.chomookun.fintics.core.asset.AssetService;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.daemon.common.AbstractTask;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DividendCollector extends AbstractTask {

    private final static String SCHEDULER_ID = "DividendCollector";

    private final BasketService basketService;

    private final AssetService assetService;

    private final DividendRepository dividendRepository;

    private final DividendClient dividendClient;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 60_000, fixedDelay = 1_000 * 60 * 60 * 24)
    public void collect() {
        log.info("DividendCollector - Start collect dividend.");
        Execution execution = startExecution(SCHEDULER_ID);
        try {
            // gets baskets
            List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();

            // collects distinct asset ids
            Set<String> assetIds = new HashSet<>();
            baskets.forEach(basket -> {
                basket.getBasketAssets().forEach(basketAsset -> {
                    assetIds.add(basketAsset.getAssetId());
                });
            });

            // updates execution
            execution.getTotalCount().set(assetIds.size());
            updateExecution(execution);

            // collect and save dividend
            for (String assetId : assetIds) {
                try {
                    Asset asset = assetService.getAsset(assetId).orElseThrow();
                    saveDividends(asset);
                    execution.getSuccessCount().incrementAndGet();
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                    execution.getFailCount().incrementAndGet();
                } finally {
                    updateExecution(execution);
                    // 블럭 당할수 있음 으로 유량 조절
                    try {
                        Thread.sleep(3_000);
                    } catch (Throwable ignore) {}
                }
            }

            // checks fail count
            if (execution.getFailCount().longValue() > execution.getTotalCount().longValue() * 0.1) {
                throw new RuntimeException("DividendCollector - Fail count is over 10%.");
            }

            // success
            successExecution(execution);

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            failExecution(execution, e);
            throw new RuntimeException(e);
        } finally {
            sendSystemNotification(execution);
        }
        log.info("DividendCollector - Complete collect dividend.");
    }

    /**
     * Saves dividends
     * @param asset asset
     */
    void saveDividends(Asset asset) {
        LocalDate dateFrom = LocalDate.now().minusYears(10);
        LocalDate dateTo = LocalDate.now();
        List<Dividend> dividends = dividendClient.getDividends(asset, dateFrom, dateTo);
        List<DividendEntity> dividendEntities = dividends.stream()
                .map(dividend -> {
                    DividendEntity dividendEntity = DividendEntity.builder()
                            .assetId(dividend.getAssetId())
                            .date(dividend.getDate())
                            .dividendPerShare(dividend.getDividendPerShare())
                            .build();
                    log.debug("dividendEntity: {}", dividendEntity);
                    return dividendEntity;
                }).toList();
        String unitName = String.format("dividendEntities[%s]", asset.getName());
        log.debug("DividendCollector - save {}:{}", unitName, dividendEntities.size());
        saveEntities(unitName, dividendEntities, transactionManager, dividendRepository);
    }

}
