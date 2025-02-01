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
        basketRepository.findAll().forEach(basketEntity ->
                basketAssetIds.addAll(basketEntity.getBasketAssets().stream()
                        .map(BasketAssetEntity::getAssetId)
                        .toList())
        );

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

                // updates asset details
                boolean needToUpdate = true;
                // 주식 중 시가 총액이 0이거나 산출 되지 않은 기업은 제외
                if (Objects.equals(asset.getType(), "STOCK")) {
                    if (asset.getMarketCap() == null || asset.getMarketCap().compareTo(BigDecimal.ZERO) <= 0) {
                        needToUpdate = false;
                    }
                }
                if (needToUpdate) {
                    assetClient.updateAsset(asset);
                }
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
