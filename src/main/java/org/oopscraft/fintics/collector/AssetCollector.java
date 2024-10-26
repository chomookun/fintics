package org.oopscraft.fintics.collector;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.fintics.client.asset.AssetClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.BasketAsset;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class AssetCollector extends AbstractCollector {

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
            updateAssetDetail();
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
        List<Asset> assets = assetClient.getAssets();
        List<AssetEntity> assetEntities = assets.stream()
                .map(asset -> AssetEntity.builder()
                        .assetId(asset.getAssetId())
                        .name(asset.getName())
                        .market(asset.getMarket())
                        .exchange(asset.getExchange())
                        .type(asset.getType())
                        .marketCap(asset.getMarketCap())
                        .build())
                .collect(Collectors.toList());
        log.info("AssetCollector - save assetEntities:{}", assetEntities.size());
        saveEntities("assetEntities", assetEntities, transactionManager, assetRepository);
    }

    /**
     * updates asset detail
     */
    void updateAssetDetail() {
        List<AssetEntity> assetEntities = assetRepository.findAll();

        // 현재 Basket 에 등록된 종목 먼저 처리
        Set<String> assetIds = new HashSet<>();
        List<BasketEntity> basketEntities = basketRepository.findAll();
        basketEntities.forEach(basketEntity -> {
            basketEntity.getBasketAssets().forEach(basketAssetEntity -> {
                assetIds.add(basketAssetEntity.getAssetId());
            });
        });

        // 부하 문제로 정렬 하지 않고 그룹핑 후 결합
        List<AssetEntity> inSetAssets = new ArrayList<>();
        List<AssetEntity> notInSetAssets = new ArrayList<>();
        for (AssetEntity assetEntity : assetEntities) {
            if (assetIds.contains(assetEntity.getAssetId())) {
                inSetAssets.add(assetEntity);
            } else {
                notInSetAssets.add(assetEntity);
            }
        }

        // inSetAssets 이 우선, 뒤에 notInSetAssets 을 concat
        assetEntities.clear();
        assetEntities.addAll(inSetAssets);
        assetEntities.addAll(notInSetAssets);

        // loop
        for(AssetEntity assetEntity : assetEntities) {
            LocalDate updatedDate = LocalDate.now();
            // check updated date
            if (assetEntity.getUpdatedDate() != null && assetEntity.getUpdatedDate().equals(updatedDate)) {
                continue;
            }
            Asset asset = Asset.from(assetEntity);
            try {
                // applies asset detail
                assetClient.applyAssetDetail(asset);

                // updates field
                assetEntity.setUpdatedDate(updatedDate);
                assetEntity.setMarketCap(asset.getMarketCap());
                assetEntity.setPer(asset.getPer());
                assetEntity.setEps(asset.getEps());
                assetEntity.setRoe(asset.getRoe());
                assetEntity.setRoa(asset.getRoa());
                assetEntity.setDividendYield(asset.getDividendYield());

                // saves entity
                saveEntities("updateAssetEntity", List.of(assetEntity), transactionManager, assetRepository);

                // force sleep (Since it is crawling, you may be blocked, so adjust the flow rate)
                Thread.sleep(1000);
            } catch (Throwable e) {
                log.warn(e.getMessage());
            }
        }
    }

}
