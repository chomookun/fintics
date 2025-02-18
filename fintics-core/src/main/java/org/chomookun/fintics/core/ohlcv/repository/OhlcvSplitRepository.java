package org.chomookun.fintics.core.ohlcv.repository;

import org.chomookun.fintics.core.ohlcv.entity.OhlcvSplitEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OhlcvSplitRepository extends JpaRepository<OhlcvSplitEntity, OhlcvSplitEntity.Pk>, JpaSpecificationExecutor<OhlcvSplitEntity> {

    @Query("select a from OhlcvSplitEntity a " +
            " where a.assetId = :assetId" +
            " and a.dateTime between :datetimeFrom and :datetimeTo" +
            " order by a.dateTime desc")
    List<OhlcvSplitEntity> findAllByAssetId(
            @Param("assetId") String assetId,
            @Param("datetimeFrom") LocalDateTime datetimeFrom,
            @Param("datetimeTo") LocalDateTime datetimeTo
    );

}
