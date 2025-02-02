package org.chomookun.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDate;
import java.util.List;

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
        try {
            log.info("OhlcvCollector - Start collect ohlcv.");
            List<Basket> baskets = basketService.getBaskets(BasketSearch.builder().build(), Pageable.unpaged()).getContent();
            for (Basket basket : baskets) {
                for (BasketAsset basketAsset : basket.getBasketAssets()) {
                    try {
                        Asset asset = assetService.getAsset(basketAsset.getAssetId()).orElseThrow();
                        saveDividends(asset);
                    } catch (Throwable e) {
                        log.warn(e.getMessage());
                    }
                }
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
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
