package org.chomookun.fintics.core.asset.repository;

import org.chomookun.fintics.core.asset.entity.AssetEntity;
import org.chomookun.fintics.core.asset.model.AssetSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AssetRepositoryCustom {

    Page<AssetEntity> findAll(AssetSearch assetSearch, Pageable pageable);

}
