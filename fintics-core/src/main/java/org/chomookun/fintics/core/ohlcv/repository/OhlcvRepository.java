package org.chomookun.fintics.core.ohlcv.repository;

import org.chomookun.fintics.core.ohlcv.entity.OhlcvEntity;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
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

    /**
     * Finds all by asset id and type
     * @param assetId asset id
     * @param type type
     * @param dateTimeFrom date time from
     * @param dateTimeTo date time to
     * @param pageable pageable
     * @return list of ohlcv
     */
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

    /**
     * Finds distinct asset ids
     * @return distinct asset ids
     */
    @Query("select distinct a.assetId from OhlcvEntity a")
    List<String> findDistinctAssetIds();

    /**
     * Deletes by asset id
     * @param assetId asset id
     */
    @Modifying
    @Query("delete from OhlcvEntity a where a.assetId = :assetId")
    void deleteByAssetId(@Param("assetId") String assetId);

}
