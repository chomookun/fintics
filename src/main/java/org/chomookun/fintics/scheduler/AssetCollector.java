package org.chomookun.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.execution.model.Execution;
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

import java.time.LocalDate;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetCollector extends AbstractScheduler {

    private final static String SCHEDULER_ID = "AssetCollector";

    private final AssetRepository assetRepository;

    private final BasketRepository basketRepository;

    private final PlatformTransactionManager transactionManager;

    private final AssetClient assetClient;

    /**
     * schedule collect
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 1_000 * 60 * 60 * 24)
    public void collect() {
        log.info("AssetCollector - Start collect asset.");
        Execution execution = startExecution(SCHEDULER_ID);
        try {
            saveAssets(execution);
            successExecution(execution);
        } catch(Throwable e) {
            log.error(e.getMessage(), e);
            failExecution(execution, e);
            throw new RuntimeException(e);
        } finally {
            sendSystemAlarm(execution);
        }
        log.info("AssetCollector - End collect asset.");
    }

    /**
     * saves assets
     */
    void saveAssets(Execution execution) {
        try {
            // Gets asset list from asset client
            List<Asset> assetsFromClient = assetClient.getAssets();

            // updates execution
            execution.getTotalCount().set(assetsFromClient.size());
            updateExecution(execution);

            // primary asset ids
            List<String> primaryAssetIds = extractPrimaryAssetIds(assetsFromClient);

            // sorting by primary asset ids
            sortByPrimaryAssetIds(assetsFromClient, primaryAssetIds);

            // merge (상장 폐지 등 삭제 종목 제외는 skip)
            for (Asset asset : assetsFromClient) {
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
                            // primary 종목은 1일
                            if (primaryAssetIds.contains(asset.getAssetId())) {
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

                        // success count
                        execution.getSuccessCount().incrementAndGet();
                    } catch (Throwable t) {
                        log.warn(t.getMessage());
                        execution.getFailCount().incrementAndGet();
                    }

                    // saves
                    saveEntities("assetEntity", List.of(assetEntity), transactionManager, assetRepository);

                    // updates execution
                    updateExecution(execution);

                } catch (Throwable t) {
                    log.warn(t.getMessage());
                    execution.getFailCount().incrementAndGet();
                }
            }

            // checks fail percentage
            if (execution.getFailCount().longValue() > execution.getTotalCount().longValue() * 0.1) {
                throw new RuntimeException("AssetCollector - Fail count is over 10%.");
            }

        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Extracts primary asset ids
     * @param assets assets
     * @return primary asset ids
     */
    List<String> extractPrimaryAssetIds(List<Asset> assets) {
        Set<String> primaryAssetIds = new LinkedHashSet<>();

        // 우선 순위 1 - basket 에 등록된 asset id 추출
        List<String> basketAssetIds = new ArrayList<>();
        basketRepository.findAll().forEach(basketEntity ->
                basketAssetIds.addAll(basketEntity.getBasketAssets().stream()
                        .map(BasketAssetEntity::getAssetId)
                        .toList())
        );
        primaryAssetIds.addAll(basketAssetIds);

        // 우선 순위 2 - favorite assets id 추출
        List<String> favoriteAssetIds = assetRepository.findAll(AssetSearch.builder().favorite(true).build(), Pageable.unpaged())
                .getContent().stream()
                .map(AssetEntity::getAssetId)
                .toList();
        primaryAssetIds.addAll(favoriteAssetIds);

        // 우선 순위 3 - ETF
        List<String> etfAssetIds = assets.stream()
                .filter(asset -> asset.getType().equals("ETF"))
                .map(Asset::getAssetId)
                .toList();
        primaryAssetIds.addAll(etfAssetIds);

        // returns
        return new ArrayList<>(primaryAssetIds);
    }

    /**
     * Sorts assets by primary asset ids
     * @param assets assets
     * @param primaryAssetIds primary asset ids
     */
    void sortByPrimaryAssetIds(List<Asset> assets, List<String> primaryAssetIds) {
        assets.sort(Comparator.comparingInt(asset -> {
            int index = primaryAssetIds.indexOf(asset.getAssetId());
            return index == -1 ? Integer.MAX_VALUE : index;
        }));
    }

}
