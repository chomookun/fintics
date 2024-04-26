package org.oopscraft.fintics.service;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.oopscraft.fintics.client.ohlcv.OhlcvClient;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.oopscraft.fintics.scheduler.PastOhlcvCollector;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataService {

    private final JPAQueryFactory jpaQueryFactory;

    private final DataMapper dataMapper;

    private final OhlcvClient ohlcvClient;

    private final AssetService assetService;

    private final AssetOhlcvRepository assetOhlcvRepository;

    private final IndiceService indiceService;

    private final IndiceOhlcvRepository indiceOhlcvRepository;

    public DataSummary getSummary() {
        List<DataSummary.AssetOhlcvStatistics> assetMinuteOhlcvStatistics = dataMapper.selectAssetOhlcvStatistics(Ohlcv.Type.MINUTE);
        List<DataSummary.AssetOhlcvStatistics> assetDailyOhlcvStatistics = dataMapper.selectAssetOhlcvStatistics(Ohlcv.Type.DAILY);
        List<DataSummary.IndiceOhlcvStatistics> indiceMinuteOhlcvStatistics = dataMapper.selectIndiceOhlcvStatistics(Ohlcv.Type.MINUTE);
        List<DataSummary.IndiceOhlcvStatistics> indiceDailyOhlcvStatistics = dataMapper.selectIndiceOhlcvStatistics(Ohlcv.Type.DAILY);
        return DataSummary.builder()
                .assetMinuteOhlcvStatistics(assetMinuteOhlcvStatistics)
                .assetDailyOhlcvStatistics(assetDailyOhlcvStatistics)
                .indiceMinuteOhlcvStatistics(indiceMinuteOhlcvStatistics)
                .indiceDailyOhlcvStatistics(indiceDailyOhlcvStatistics)
                .build();
    }

    public List<Asset> getAssets(String assetId, String assetName, String market, Pageable pageable) {
        QAssetEntity qAssetEntity = QAssetEntity.assetEntity;
        List<AssetEntity> assetEntities = jpaQueryFactory
                .selectFrom(qAssetEntity)
                .where(
                        Optional.ofNullable(assetId).map(qAssetEntity.assetId::contains).orElse(null),
                        Optional.ofNullable(assetName).map(qAssetEntity.assetName::contains).orElse(null),
                        Optional.ofNullable(market).map(qAssetEntity.market::eq).orElse(null)
                )
                .orderBy(qAssetEntity.marketCap.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();
        return assetEntities.stream()
                .map(Asset::from)
                .collect(Collectors.toList());
    }

    public List<DataSummary.AssetOhlcv> getAssetOhlcvs(String assetId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Boolean interpolated, Pageable pageable) {
        QAssetOhlcvEntity qAssetOhlcvEntity = QAssetOhlcvEntity.assetOhlcvEntity;
        List<AssetOhlcvEntity> assetOhlcvEntities = jpaQueryFactory
                .selectFrom(qAssetOhlcvEntity)
                .where(
                        Optional.ofNullable(assetId).map(qAssetOhlcvEntity.assetId::contains).orElse(null),
                        Optional.ofNullable(type).map(qAssetOhlcvEntity.type::eq).orElse(null),
                        Optional.ofNullable(dateTimeFrom).map(qAssetOhlcvEntity.dateTime::goe).orElse(null),
                        Optional.ofNullable(dateTimeTo).map(qAssetOhlcvEntity.dateTime::loe).orElse(null),
                        Optional.ofNullable(interpolated).map(qAssetOhlcvEntity.interpolated::eq).orElse(null)
                )
                .orderBy(qAssetOhlcvEntity.dateTime.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();
        return assetOhlcvEntities.stream()
                .map(DataSummary.AssetOhlcv::from)
                .collect(Collectors.toList());
    }

    public List<DataSummary.IndiceOhlcv> getIndiceOhlcvs(Indice.Id indiceId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Boolean interpolated, Pageable pageable) {
        QIndiceOhlcvEntity qIndiceOhlcvEntity = QIndiceOhlcvEntity.indiceOhlcvEntity;
        List<IndiceOhlcvEntity> indiceOhlcvEntities = jpaQueryFactory
                .selectFrom(qIndiceOhlcvEntity)
                .where(
                        Optional.ofNullable(indiceId).map(qIndiceOhlcvEntity.indiceId::eq).orElse(null),
                        Optional.ofNullable(type).map(qIndiceOhlcvEntity.type::eq).orElse(null),
                        Optional.ofNullable(dateTimeFrom).map(qIndiceOhlcvEntity.dateTime::goe).orElse(null),
                        Optional.ofNullable(dateTimeTo).map(qIndiceOhlcvEntity.dateTime::loe).orElse(null),
                        Optional.ofNullable(interpolated).map(qIndiceOhlcvEntity.interpolated::eq).orElse(null)
                )
                .orderBy(qIndiceOhlcvEntity.dateTime.desc())
                .limit(pageable.getPageSize())
                .offset(pageable.getOffset())
                .fetch();
        return indiceOhlcvEntities.stream()
                .map(DataSummary.IndiceOhlcv::from)
                .collect(Collectors.toList());

    }

    @Transactional
    public void interpolateAssetOhlcvs(String assetId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        Asset asset = assetService.getAsset(assetId).orElseThrow();
        List<Ohlcv> ohlcvs = ohlcvClient.getAssetOhlcvs(asset, type, dateTimeFrom, dateTimeTo);
        for (Ohlcv ohlcv : ohlcvs) {
            AssetOhlcvEntity.Pk pk = AssetOhlcvEntity.Pk.builder()
                    .assetId(asset.getAssetId())
                    .type(ohlcv.getType())
                    .dateTime(ohlcv.getDateTime())
                    .build();
            AssetOhlcvEntity assetOhlcvEntity = assetOhlcvRepository.findById(pk).orElse(null);
            if (assetOhlcvEntity == null) {
                assetOhlcvEntity = AssetOhlcvEntity.builder()
                        .assetId(asset.getAssetId())
                        .type(ohlcv.getType())
                        .dateTime(ohlcv.getDateTime())
                        .openPrice(ohlcv.getOpenPrice())
                        .highPrice(ohlcv.getHighPrice())
                        .lowPrice(ohlcv.getLowPrice())
                        .closePrice(ohlcv.getClosePrice())
                        .volume(ohlcv.getVolume())
                        .interpolated(ohlcv.isInterpolated())
                        .build();
                assetOhlcvRepository.saveAndFlush(assetOhlcvEntity);
            }
        }
    }

    @Transactional
    public void interpolateIndiceOhlcvs(Indice.Id indiceId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo) {
        Indice indice = indiceService.getIndice(indiceId.name()).orElseThrow();
        List<Ohlcv> ohlcvs = ohlcvClient.getIndiceOhlcvs(indice, type, dateTimeFrom, dateTimeTo);
        for (Ohlcv ohlcv : ohlcvs) {
            IndiceOhlcvEntity.Pk pk = IndiceOhlcvEntity.Pk.builder()
                    .indiceId(indice.getIndiceId())
                    .type(ohlcv.getType())
                    .dateTime(ohlcv.getDateTime())
                    .build();
            IndiceOhlcvEntity indiceOhlcvEntity = indiceOhlcvRepository.findById(pk).orElse(null);
            if (indiceOhlcvEntity == null) {
                indiceOhlcvEntity = IndiceOhlcvEntity.builder()
                        .indiceId(indice.getIndiceId())
                        .type(ohlcv.getType())
                        .dateTime(ohlcv.getDateTime())
                        .openPrice(ohlcv.getOpenPrice())
                        .highPrice(ohlcv.getHighPrice())
                        .lowPrice(ohlcv.getLowPrice())
                        .closePrice(ohlcv.getClosePrice())
                        .volume(ohlcv.getVolume())
                        .interpolated(ohlcv.isInterpolated())
                        .build();
                indiceOhlcvRepository.saveAndFlush(indiceOhlcvEntity);
            }
        }
    }

}
