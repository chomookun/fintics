package org.chomookun.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.client.asset.AssetClient;
import org.chomookun.fintics.dao.AssetEntity;
import org.chomookun.fintics.dao.AssetRepository;
import org.chomookun.fintics.dao.BasketAssetEntity;
import org.chomookun.fintics.dao.BasketRepository;
import org.chomookun.fintics.model.Asset;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        // basket 에 등록된 asset id 추출
        List<String> basketAssetIds = new ArrayList<>();
        basketRepository.findAll().forEach(basketEntity -> {
            basketAssetIds.addAll(basketEntity.getBasketAssets().stream()
                    .map(BasketAssetEntity::getAssetId)
                    .toList());
        });

        // AssetClient 로 부터 Assets 조회
        List<Asset> assetsFromClient = assetClient.getAssets();

        // basket 에 등록된 Asset 우선 처리 하도록 List 조합
        List<Asset> assetsInBasket = new ArrayList<>();
        List<Asset> assetsNotInBasket = new ArrayList<>();
        assetsFromClient.forEach(it -> {
            if (basketAssetIds.contains(it.getAssetId())) {
                assetsInBasket.add(it);
            } else {
                assetsNotInBasket.add(it);
            }
        });
        List<Asset> assets = new ArrayList<>();
        assets.addAll(assetsInBasket);
        assets.addAll(assetsNotInBasket);

        // merge (상장 폐지 등 삭제 종목 제외는 skip)
        for (Asset asset : assets) {
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

                // gets asset details
                Map<String, String> assetDetail = assetClient.getAssetDetail(asset);
                assetEntity.setUpdatedDate(LocalDate.now());
                assetEntity.setMarketCap(Optional.ofNullable(assetDetail.get("marketCap")).map(BigDecimal::new).orElse(null));
                assetEntity.setEps(Optional.ofNullable(assetDetail.get("eps")).map(BigDecimal::new).orElse(null));
                assetEntity.setRoe(Optional.ofNullable(assetDetail.get("roe")).map(BigDecimal::new).orElse(null));
                assetEntity.setPer(Optional.ofNullable(assetDetail.get("per")).map(BigDecimal::new).orElse(null));
                assetEntity.setDividendFrequency(Optional.ofNullable(assetDetail.get("dividendFrequency")).map(Integer::parseInt).orElse(null));
                assetEntity.setDividendYield(Optional.ofNullable(assetDetail.get("dividendYield")).map(BigDecimal::new).orElse(null));
                assetEntity.setCapitalGain(Optional.ofNullable(assetDetail.get("capitalGain")).map(BigDecimal::new).orElse(null));
                assetEntity.setTotalReturn(Optional.ofNullable(assetDetail.get("totalReturn")).map(BigDecimal::new).orElse(null));

                // saves
                saveEntities("assetEntity", List.of(assetEntity), transactionManager, assetRepository);
            } catch (Throwable t) {
                log.warn(t.getMessage());
            } finally {
                // 블럭 당할수 있음 으로 유량 조절
                try {
                    Thread.sleep(3_000);
                } catch(Throwable ignore){}
            }
        }
    }

}
