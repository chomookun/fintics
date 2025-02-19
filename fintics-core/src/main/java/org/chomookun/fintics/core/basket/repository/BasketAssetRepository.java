package org.chomookun.fintics.core.basket.repository;

import org.chomookun.fintics.core.basket.entity.BasketAssetEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BasketAssetRepository extends JpaRepository<BasketAssetEntity, BasketAssetEntity.Pk>, JpaSpecificationExecutor<BasketAssetEntity> {

}
