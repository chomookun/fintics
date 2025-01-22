package org.chomoo.fintics.dao;

import org.chomoo.fintics.model.Ohlcv;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OhlcvRepository extends JpaRepository<OhlcvEntity, OhlcvEntity.Pk>, JpaSpecificationExecutor<OhlcvEntity> {

    @Query("select a from OhlcvEntity a " +
            " where a.assetId = :assetId" +
            " and a.type = :type" +
            " and a.dateTime between :dateTimeFrom and :dateTimeTo" +
            " order by a.dateTime desc")
    List<OhlcvEntity> findAllByAssetIdAndType(
            @Param("assetId") String assetId,
            @Param("type") Ohlcv.Type type,
            @Param("dateTimeFrom") LocalDateTime dateTimeFrom,
            @Param("dateTimeTo") LocalDateTime dateTimeTo,
            Pageable pageable
    );

    @Query("select distinct a.assetId from OhlcvEntity a")
    List<String> findDistinctAssetIds();

    @Modifying
    @Query("delete from OhlcvEntity a where a.assetId = :assetId")
    void deleteByAssetId(@Param("assetId") String assetId);

}
