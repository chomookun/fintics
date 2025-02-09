package org.chomookun.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.chomookun.fintics.client.asset.AssetClient;
import org.chomookun.fintics.dao.AssetEntity;
import org.chomookun.fintics.dao.AssetRepository;
import org.chomookun.fintics.dao.BasketAssetEntity;
import org.chomookun.fintics.dao.BasketRepository;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.AssetSearch;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetCollector extends AbstractScheduler {

    private final AssetRepository assetRepository;

    private final BasketRepository basketRepository;

    private final PlatformTransactionManager transactionManager;

    private final AssetClient assetClient;

    /**
     * schedule collect
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 86_400_000)
    public void collect() {
        try {
            log.info("AssetCollector - Start collect asset.");
            saveAssets();
            log.info("AssetCollector - End collect asset");
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            sendSystemAlarm(this.getClass(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * saves assets
     */
    void saveAssets() {
        Instant start = Instant.now();
        long totalCount = 0;
        long failCount = 0;
        Boolean result = null;
        String error = null;
        try {
            log.info("AssetCollector - Start collect assets.");

            // basket 에 등록된 asset id 추출
            List<String> basketAssetIds = new ArrayList<>();
            basketRepository.findAll().forEach(basketEntity ->
                    basketAssetIds.addAll(basketEntity.getBasketAssets().stream()
                            .map(BasketAssetEntity::getAssetId)
                            .toList())
            );

            // favorite assets id 추출
            List<String> favoriteAssetIds = assetRepository.findAll(AssetSearch.builder().favorite(true).build(), Pageable.unpaged())
                    .getContent().stream()
                    .map(AssetEntity::getAssetId)
                    .toList();

            // Gets asset list from asset client
            List<Asset> assetsFromClient = assetClient.getAssets();

            // basket 에 등록된 Asset 우선 처리 하도록 List 조합
            List<Asset> assetsInBasket = new ArrayList<>();
            List<Asset> assetsWithFavorite = new ArrayList<>();
            List<Asset> assetsOthers = new ArrayList<>();
            assetsFromClient.forEach(it -> {
                if (basketAssetIds.contains(it.getAssetId())) {
                    assetsInBasket.add(it);
                } else if (favoriteAssetIds.contains(it.getAssetId())) {
                    assetsWithFavorite.add(it);
                }else {
                    assetsOthers.add(it);
                }
            });
            List<Asset> assets = new ArrayList<>();
            assets.addAll(assetsInBasket);      // 현재 거래 중인 종목
            assets.addAll(assetsWithFavorite);  // 관심 종목
            assets.addAll(assetsOthers);        // 그외 종목

            // merge (상장 폐지 등 삭제 종목 제외는 skip)
            for (Asset asset : assets) {
                totalCount++;

                // Updates financial details
                try {
                    AssetEntity assetEntity = assetRepository.findById(asset.getAssetId()).orElse(null);
                    if (assetEntity == null) {
                        assetEntity = AssetEntity.builder()
                                .assetId(asset.getAssetId())
                                .build();
                    }
                    assetEntity.setName(asset.getName());
                    assetEntity.setMarket(asset.getMarket());
                    assetEntity.setExchange(asset.getExchange());
                    assetEntity.setType(asset.getType());

                    // populate asset
                    try {
                        // Checks skip
                        boolean needToUpdate = true;

                        // 최근 갱신 된 경우는 제외 (대상 건이 너무 많음)
                        if (assetEntity.getUpdatedDate() != null) {
                            LocalDate expireUpdateDate;
                            // 현재 거래 중인 종목은 1일
                            if (basketAssetIds.contains(asset.getAssetId())) {
                                expireUpdateDate = LocalDate.now().minusDays(1);
                            }
                            // 그외 종목은 7일
                            else {
                                expireUpdateDate = LocalDate.now().minusDays(7);
                            }
                            // 갱신 일자가 최근인 경우 skip
                            if (assetEntity.getUpdatedDate().isAfter(expireUpdateDate)) {
                                needToUpdate = false;
                            }
                        }

                        // neet to update
                        if (needToUpdate) {
                            // populate asset
                            assetClient.populateAsset(asset);
                            // sets properties
                            assetEntity.setUpdatedDate(LocalDate.now());
                            assetEntity.setPrice(asset.getPrice());
                            assetEntity.setVolume(asset.getVolume());
                            assetEntity.setMarketCap(asset.getMarketCap());
                            assetEntity.setEps(asset.getEps());
                            assetEntity.setRoe(asset.getRoe());
                            assetEntity.setPer(asset.getPer());
                            assetEntity.setDividendFrequency(asset.getDividendFrequency());
                            assetEntity.setDividendYield(asset.getDividendYield());
                            assetEntity.setCapitalGain(asset.getCapitalGain());
                            assetEntity.setTotalReturn(asset.getTotalReturn());

                            // 블럭 당할수 있음 으로 유량 조절 (skip 인 경우는 그냥 진행 해야 됨으로 finally 에 작성 하지 않음)
                            try {
                                Thread.sleep(3_000);
                            } catch (Throwable ignore) {}
                        }
                    } catch (Throwable t) {
                        log.warn(t.getMessage());
                        failCount++;
                    }

                    // saves
                    saveEntities("assetEntity", List.of(assetEntity), transactionManager, assetRepository);

                } catch (Throwable t) {
                    log.warn(t.getMessage());
                    failCount++;
                }
            }

            // checks fail percentage
            if (failCount > totalCount * 0.1) {
                throw new RuntimeException("AssetCollector - Fail count is over 10%.");
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
            message.append("AssetCollector - Complete collect asset.").append('\n');
            message.append(String.format("- elapsed: %d:%02d:%02d", elapsed.toHoursPart(), elapsed.toMinutesPart(), elapsed.toSecondsPart())).append('\n');
            message.append(String.format("- totalCount: %d", totalCount)).append('\n');
            message.append(String.format("- failCount: %d", failCount)).append('\n');
            message.append(String.format("- result: %s", result)).append('\n');
            message.append(String.format("- error: %s", error)).append('\n');
            log.info(message.toString());
            sendSystemAlarm(this.getClass(), message.toString());
            log.info("AssetCollector - Complete collect asset.");
        }
    }

}
