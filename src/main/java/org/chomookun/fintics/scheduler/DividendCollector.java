package org.chomookun.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.chomookun.fintics.client.dividend.DividendClient;
import org.chomookun.fintics.dao.DividendEntity;
import org.chomookun.fintics.dao.DividendRepository;
import org.chomookun.fintics.model.*;
import org.chomookun.fintics.service.AssetService;
import org.chomookun.fintics.service.BasketService;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class DividendCollector extends AbstractScheduler {

    private final BasketService basketService;

    private final AssetService assetService;

    private final DividendRepository dividendRepository;

    private final DividendClient dividendClient;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 60_000, fixedDelay = 86_400_000)
    public void collect() {
        Instant start = Instant.now();
        long totalCount = 0;
        long failCount = 0;
        Boolean result = null;
        String error = null;
        try {
            log.info("DividendCollector - Start collect dividend.");

            // gets baskets
            List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();

            // collects distinct asset ids
            Set<String> assetIds = new HashSet<>();
            baskets.forEach(basket -> {
                basket.getBasketAssets().forEach(basketAsset -> {
                    assetIds.add(basketAsset.getAssetId());
                });
            });

            // collect and save dividend
            for (String assetId : assetIds) {
                totalCount ++;
                try {
                    Asset asset = assetService.getAsset(assetId).orElseThrow();
                    saveDividends(asset);
                } catch (Throwable e) {
                    log.warn(e.getMessage());
                    failCount ++;
                } finally {
                    // 블럭 당할수 있음 으로 유량 조절
                    try {
                        Thread.sleep(3_000);
                    } catch (Throwable ignore) {}
                }
            }

            // checks fail count
            if (failCount > totalCount * 0.1) {
                throw new RuntimeException("DividendCollector - Fail count is over 10%.");
            }

            // result
            result = true;

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            result = false;
            error = ExceptionUtils.getRootCauseMessage(e);
            throw new RuntimeException(e);
        } finally {
            // send message
            Duration elapsed = Duration.between(start, Instant.now());
            StringBuilder message = new StringBuilder();
            message.append("=".repeat(80)).append('\n');
            message.append("DividendCollector - Complete collect dividend.").append('\n');
            message.append(String.format("- elapsed: %02d:%02d:%02d", elapsed.toHoursPart(), elapsed.toMinutesPart(), elapsed.toSecondsPart())).append('\n');
            message.append(String.format("- totalCount: %d", totalCount)).append('\n');
            message.append(String.format("- failCount: %d", failCount)).append('\n');
            message.append(String.format("- result: %s", result)).append('\n');
            message.append(String.format("- error: %s", error)).append('\n');
            message.append("=".repeat(80)).append('\n');
            log.info(message.toString());
            sendSystemAlarm(this.getClass(), message.toString());
            log.info("DividendCollector - Complete collect dividend.");
        }
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
