package org.chomookun.fintics.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.dao.OhlcvRepository;
import org.chomookun.fintics.dao.OhlcvSplitEntity;
import org.chomookun.fintics.dao.OhlcvSplitRepository;
import org.chomookun.fintics.model.Ohlcv;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

/**
 * ohlcv service
 */
@Component
@RequiredArgsConstructor
public class OhlcvService {

    private final OhlcvRepository ohlcvRepository;

    private final OhlcvSplitRepository ohlcvSplitRepository;

    /**
     * returns daily ohlcvs
     * @param assetId asset id
     * @param dateTimeFrom date time from
     * @param dateTimeTo date time to
     * @param pageable pageable
     * @return list of daily ohlcvs
     */
    public List<Ohlcv> getDailyOhlcvs(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        // daily ohlcv entities
        List<Ohlcv> dailyOhlcvs = ohlcvRepository.findAllByAssetIdAndType(assetId, Ohlcv.Type.DAILY, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();

        // apply split ratio
        applySplitRatioIfExist(assetId, dailyOhlcvs);

        // return
        return dailyOhlcvs;
    }

    /**
     * returns minute ohlcvs
     * @param assetId asset id
     * @param dateTimeFrom date time from
     * @param dateTimeTo date time to
     * @param pageable pageable
     * @return list of minute ohlcvs
     */
    public List<Ohlcv> getMinuteOhlcvs(String assetId, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo, Pageable pageable) {
        /// gets minute ohlcvs from entity
        List<Ohlcv> minuteOhlcvs = ohlcvRepository.findAllByAssetIdAndType(assetId, Ohlcv.Type.MINUTE, dateTimeFrom, dateTimeTo, pageable).stream()
                .map(Ohlcv::from)
                .toList();

        // apply split ratio
        applySplitRatioIfExist(assetId, minuteOhlcvs);

        // return
        return minuteOhlcvs;
    }

    /**
     * applies split ratio to ohlcvs
     * @param assetId asset id
     * @param ohlcvs ohlcvs
     */
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

    /**
     * calculates cumulative ratio as navigable map
     * @param splitEntities asset split entities
     * @return return ratio navigable map
     */
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

    /**
     * return cumulative ratio
     * @param dateTime date time
     * @param cumulativeRatios cumulative ratios map
     * @return cumulative ratio
     */
    BigDecimal getCumulativeRatioForDate(LocalDateTime dateTime, NavigableMap<LocalDateTime, BigDecimal> cumulativeRatios) {
        return cumulativeRatios.tailMap(dateTime, false).values().stream()
                .reduce(BigDecimal.ONE, BigDecimal::multiply);
    }

}
