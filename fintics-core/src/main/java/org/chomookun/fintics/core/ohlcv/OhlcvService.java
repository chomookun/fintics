package org.chomookun.fintics.core.ohlcv;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.asset.AssetService;
import org.chomookun.fintics.core.ohlcv.client.OhlcvClient;
import org.chomookun.fintics.core.ohlcv.repository.OhlcvRepository;
import org.chomookun.fintics.core.ohlcv.entity.OhlcvSplitEntity;
import org.chomookun.fintics.core.ohlcv.repository.OhlcvSplitRepository;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class OhlcvService {

    private final OhlcvRepository ohlcvRepository;

    private final OhlcvSplitRepository ohlcvSplitRepository;

    private final AssetService assetService;

    private final OhlcvClient ohlcvClient;

    public List<Ohlcv> getOhlcvs(String assetId, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        // daily ohlcv entities
        List<Ohlcv> ohlcvs = ohlcvRepository.findAllByAssetIdAndType(assetId, type, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();
        // ohlcv client
        if (ohlcvs.isEmpty()) {
            Asset asset = assetService.getAsset(assetId).orElseThrow();
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

}
