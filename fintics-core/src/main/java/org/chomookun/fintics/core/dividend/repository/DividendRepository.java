package org.chomookun.fintics.core.dividend.repository;

import org.chomookun.fintics.core.dividend.entity.DividendEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DividendRepository extends JpaRepository<DividendEntity, DividendEntity.Pk> {

    List<DividendEntity> findByAssetIdAndDateBetweenOrderByDateDesc(String assetId, LocalDate dateFrom, LocalDate dateTo);

}
