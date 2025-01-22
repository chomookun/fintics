package org.chomoo.fintics.service;

import lombok.RequiredArgsConstructor;
import org.chomoo.fintics.dao.AssetEntity;
import org.chomoo.fintics.dao.AssetRepository;
import org.chomoo.fintics.model.Asset;
import org.chomoo.fintics.model.AssetSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * asset service
 */
@Service
@RequiredArgsConstructor
public class AssetService {

    private final AssetRepository assetRepository;

    /**
     * gets asset list
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
     * gets specified asset
     * @param assetId asset id
     * @return asset
     */
    public Optional<Asset> getAsset(String assetId) {
        return assetRepository.findById(assetId)
                .map(Asset::from);
    }

}
