package org.chomookun.fintics.core.asset;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.asset.client.AssetClient;
import org.chomookun.fintics.core.asset.entity.AssetEntity;
import org.chomookun.fintics.core.asset.repository.AssetRepository;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.asset.model.AssetSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * asset service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetRepository assetRepository;

    private final AssetClient assetClient;

    /**
     * Gets asset list
     * @param assetSearch asset search condition
     * @param pageable pageable
     * @return assets
     */
    public Page<Asset> getAssets(AssetSearch assetSearch, Pageable pageable) {
        Page<AssetEntity> assetEntityPage = assetRepository.findAll(assetSearch, pageable);
        List<Asset> assets = assetEntityPage.getContent().stream()
                .map(Asset::from)
                .toList();
        long total = assetEntityPage.getTotalElements();
        return new PageImpl<>(assets, pageable, total);
    }

    /**
     * Gets specified asset
     * @param assetId asset id
     * @return asset
     */
    public Optional<Asset> getAsset(String assetId) {
        Asset asset = assetRepository.findById(assetId)
                .map(Asset::from)
                .orElse(null);
        // check updated date (last updated date is older than 1 week)
        if (asset != null) {
            LocalDate updatedDate = asset.getUpdatedDate();
            if (updatedDate == null || updatedDate.isBefore(LocalDate.now().minusWeeks(1))) {
                try {
                    assetClient.populateAsset(asset);
                } catch (Throwable ignore) {
                    log.warn("failed to populate asset: {}", assetId);
                }
            }
        }
        return Optional.ofNullable(asset);
    }

    /**
     * Sets favorite
     * @param assetId asset id
     * @param favorite  favorite
     */
    public void setFavorite(String assetId, boolean favorite) {
        AssetEntity assetEntity = assetRepository.findById(assetId).orElseThrow();
        assetEntity.setFavorite(favorite);
        assetRepository.save(assetEntity);
    }

}
