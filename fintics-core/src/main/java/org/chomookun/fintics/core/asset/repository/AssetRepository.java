package org.chomookun.fintics.core.asset.repository;

import org.chomookun.fintics.core.asset.entity.AssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetRepository extends JpaRepository<AssetEntity, String>, JpaSpecificationExecutor<AssetEntity>, AssetRepositoryCustom {

}
