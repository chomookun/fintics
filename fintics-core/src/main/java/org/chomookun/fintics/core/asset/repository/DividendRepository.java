package org.chomookun.fintics.core.asset.repository;

import org.chomookun.fintics.core.asset.entity.DividendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, DividendEntity.Pk> {

    List<DividendEntity> findByAssetIdAndDateBetweenOrderByDateDesc(String assetId, LocalDate dateFrom, LocalDate dateTo);

}
