package org.chomookun.fintics.core.asset;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.asset.client.asset.AssetClient;
import org.chomookun.fintics.core.asset.client.dividend.DividendClient;
import org.chomookun.fintics.core.asset.client.ohlcv.OhlcvClient;
import org.chomookun.fintics.core.asset.entity.AssetEntity;
import org.chomookun.fintics.core.asset.entity.OhlcvSplitEntity;
import org.chomookun.fintics.core.asset.model.Dividend;
import org.chomookun.fintics.core.asset.model.Ohlcv;
import org.chomookun.fintics.core.asset.repository.AssetRepository;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.asset.model.AssetSearch;
import org.chomookun.fintics.core.asset.repository.DividendRepository;
import org.chomookun.fintics.core.asset.repository.OhlcvRepository;
import org.chomookun.fintics.core.asset.repository.OhlcvSplitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetService {

    private final AssetRepository assetRepository;

    private final AssetClient assetClient;

    private final OhlcvRepository ohlcvRepository;

    private final OhlcvSplitRepository ohlcvSplitRepository;

    private final OhlcvClient ohlcvClient;

    private final DividendRepository dividendRepository;

    private final DividendClient dividendClient;

    /**
     * Returns assets
     * @param assetSearch asset search
     * @param pageable pageable
     * @return page of asset
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
     * Returns asset detail
     * @param assetId asset id
     * @return asset detail
     */
    public Optional<Asset> getAsset(String assetId) {
        Asset asset = assetRepository.findById(assetId)
                .map(Asset::from)
                .orElse(null);
        // check updated date (last updated date is older than 1 week)
        if (asset != null) {
            LocalDate updatedDate = asset.getUpdatedDate();
            if (updatedDate == null || updatedDate.isBefore(LocalDate.now().minusWeeks(1))) {
                try {
                    assetClient.populateAsset(asset);
                } catch (Throwable ignore) {
                    log.warn("failed to populate asset: {}", assetId);
                }
            }
        }
        return Optional.ofNullable(asset);
    }

    /**
     * Sets favorite
     * @param assetId asset id
     * @param favorite favorite
     */
    public void setFavorite(String assetId, boolean favorite) {
        AssetEntity assetEntity = assetRepository.findById(assetId).orElseThrow();
        assetEntity.setFavorite(favorite);
        assetRepository.save(assetEntity);
    }

    /**
     * Returns asset ohlcvs
     * @param assetId asset id
     * @param type type
     * @param dateTimeFrom from
     * @param dateTimeTo to
     * @param pageable pageable
     * @return list of ohlcv
     */
    public List<Ohlcv> getOhlcvs(String assetId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        // daily ohlcv entities
        List<Ohlcv> ohlcvs = ohlcvRepository.findAllByAssetIdAndType(assetId, type, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();
        // ohlcv client
        if (ohlcvs.isEmpty()) {
            Asset asset = getAsset(assetId).orElseThrow();
            ohlcvs = ohlcvClient.getOhlcvs(asset, type, dateTimeFrom, dateTimeTo);
            // apply pageable (client not support pagination)
            if (pageable.isPaged()) {
                long startIndex = pageable.getOffset();
                long endIndex = Math.min(ohlcvs.size(), startIndex + pageable.getPageSize());
                ohlcvs = ohlcvs.subList(Math.toIntExact(startIndex), Math.toIntExact(endIndex));
            }
        }
        // apply split ratio
        applySplitRatioIfExist(assetId, ohlcvs);
        // return
        return ohlcvs;
    }

    void applySplitRatioIfExist(String assetId, List<Ohlcv> ohlcvs) {
        // if ohlcvs is empty, skip
        if (ohlcvs.isEmpty()) {
            return;
        }
        // ohlcv split data
        LocalDateTime dateTimeFrom = ohlcvs.stream()
                .map(Ohlcv::getDateTime)
                .min(Comparator.naturalOrder())
                .orElseThrow();
        LocalDateTime dateTimeTo = ohlcvs.stream()
                .map(Ohlcv::getDateTime)
                .max(Comparator.naturalOrder())
                .orElseThrow();
        List<OhlcvSplitEntity> ohlcvSplitEntities = ohlcvSplitRepository.findAllByAssetId(assetId, dateTimeFrom, dateTimeTo);
        // if split data exists
        if (!ohlcvSplitEntities.isEmpty()) {
            // prepare split ratio map
            NavigableMap<LocalDateTime, BigDecimal> cumulativeRatios = calculateCumulativeRatios(ohlcvSplitEntities);
            // adjust split to ohlcv
            for (Ohlcv ohlcv : ohlcvs) {
                BigDecimal splitRatio = getCumulativeRatioForDate(ohlcv.getDateTime(), cumulativeRatios);
                ohlcv.setOpen(ohlcv.getOpen().divide(splitRatio, MathContext.DECIMAL32));
                ohlcv.setHigh(ohlcv.getHigh().divide(splitRatio, MathContext.DECIMAL32));
                ohlcv.setLow(ohlcv.getLow().divide(splitRatio, MathContext.DECIMAL32));
                ohlcv.setClose(ohlcv.getClose().divide(splitRatio, MathContext.DECIMAL32));
                ohlcv.setVolume(ohlcv.getVolume().multiply(splitRatio));
            }
        }
    }

    NavigableMap<LocalDateTime, BigDecimal> calculateCumulativeRatios(List<OhlcvSplitEntity> splitEntities) {
        NavigableMap<LocalDateTime, BigDecimal> cumulativeRatios = new TreeMap<>();
        BigDecimal cumulativeRatio = BigDecimal.ONE;
        for (OhlcvSplitEntity split : splitEntities) {
            BigDecimal splitRatio = BigDecimal.ONE;
            // forward split
            if (split.getSplitTo().compareTo(split.getSplitFrom()) > 0) {
                splitRatio = split.getSplitTo().divide(split.getSplitFrom(), MathContext.DECIMAL32);
            }
            // reverse split
            if (split.getSplitTo().compareTo(split.getSplitFrom()) < 0) {
                splitRatio = split.getSplitTo().multiply(split.getSplitFrom());
            }
            cumulativeRatio = cumulativeRatio.multiply(splitRatio);
            cumulativeRatios.put(split.getDateTime(), cumulativeRatio);
        }
        return cumulativeRatios;
    }

    BigDecimal getCumulativeRatioForDate(LocalDateTime dateTime, NavigableMap<LocalDateTime, BigDecimal> cumulativeRatios) {
        return cumulativeRatios.tailMap(dateTime, false).values().stream()
                .reduce(BigDecimal.ONE, BigDecimal::multiply);
    }

    /**
     * Returns dividends
     * @param assetId asset id
     * @param dateFrom from
     * @param dateTo to
     * @param pageable pageable
     * @return list of dividend
     */
    public List<Dividend> getDividends(String assetId, LocalDate dateFrom, LocalDate dateTo, Pageable pageable) {
        List<Dividend> dividends = dividendRepository.findByAssetIdAndDateBetweenOrderByDateDesc(assetId, dateFrom, dateTo).stream()
                .map(Dividend::from)
                .toList();
        // dividend client
        if (dividends.isEmpty()) {
            Asset asset = getAsset(assetId).orElseThrow();
            dividends = dividendClient.getDividends(asset, dateFrom, dateTo);
            // apply pageable (client not support pagination)
            if (pageable.isPaged()) {
                long startIndex = pageable.getOffset();
                long endIndex = Math.min(dividends.size(), startIndex + pageable.getPageSize());
                dividends = dividends.subList(Math.toIntExact(startIndex), Math.toIntExact(endIndex));
            }
        }
        // returns
        return dividends;
    }

}
