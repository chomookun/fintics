package org.chomookun.fintics.core.trade.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.trade.entity.TradeAssetEntity;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

/**
 * trade asset
 */
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAsset extends Asset {

    private String tradeId;

    private LocalDateTime dateTime;

    private BigDecimal previousClose;

    private BigDecimal open;

    private BigDecimal close;

    private BigDecimal volume;

    private List<Ohlcv> dailyOhlcvs;

    private List<Ohlcv> minuteOhlcvs;

    private String message;

    @Builder.Default
    private Map<String,Object> context = new HashMap<>();

    /**
     * gets net change
     * @return net change
     */
    public BigDecimal getNetChange() {
        return (close != null ? close : BigDecimal.ZERO)
                .subtract(previousClose != null ? previousClose : BigDecimal.ZERO);
    }

    /**
     * gets net change percentage
     * @return net change percentage
     */
    public BigDecimal getNetChangePercentage() {
        if (previousClose == null || previousClose.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // if previous close not existed
        }
        return getNetChange()
                .divide(previousClose, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * gets intraday net change
     * @return intraday net change
     */
    public BigDecimal getIntraDayNetChange() {
        return (close != null ? close : BigDecimal.ZERO)
                .subtract(open != null ? open : BigDecimal.ZERO);
    }

    /**
     * gets intraday net change percentage
     * @return intraday net change percentage
     */
    public BigDecimal getIntraDayNetChangePercentage() {
        if (open == null || open.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO; // if open is not existed
        }
        return getIntraDayNetChange()
                .divide(open, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    /**
     * returns OHLCV list
     * @param type ohlcv type
     * @param period period
     * @return ohlcvs
     */
    public List<Ohlcv> getOhlcvs(Ohlcv.Type type, int period) {
        List<Ohlcv> ohlcvs;
        switch(type) {
            case MINUTE -> ohlcvs = resampleOhlcvs(minuteOhlcvs, period);
            case DAILY -> ohlcvs = resampleOhlcvs(dailyOhlcvs, period);
            default -> throw new IllegalArgumentException("invalid Ohlcv type");
        }
        return Collections.unmodifiableList(ohlcvs);
    }

    public List<Ohlcv> getOhlcvs(String type, int period) {
        return getOhlcvs(Ohlcv.Type.valueOf(type), period);
    }

    /**
     * resample ohlcvs by period
     * @param ohlcvs ohlcvs
     * @param period period
     * @return resampled ohlcvs
     */
    private List<Ohlcv> resampleOhlcvs(List<Ohlcv> ohlcvs, int period) {
        if (ohlcvs.isEmpty() || period <= 0) {
            return Collections.emptyList();
        }

        List<Ohlcv> resampledOhlcvs = new ArrayList<>();
        int dataSize = ohlcvs.size();
        int currentIndex = 0;

        while (currentIndex < dataSize) {
            int endIndex = Math.min(currentIndex + period, dataSize);
            List<Ohlcv> subList = ohlcvs.subList(currentIndex, endIndex);
            Ohlcv resampledData = createResampledOhlcv(subList);
            resampledOhlcvs.add(resampledData);
            currentIndex += period;
        }

        return resampledOhlcvs;
    }

    /**
     * creates resampled ohlcvs
     * @param ohlcvs ohlcvs
     * @return resampled ohlcvs
     */
    private Ohlcv createResampledOhlcv(List<Ohlcv> ohlcvs) {
        // convert to series
        List<Ohlcv> series = new ArrayList<>(ohlcvs);
        Collections.reverse(series);

        // merge ohlcv
        Ohlcv.Type type = null;
        LocalDateTime datetime = null;
        ZoneId timezone = null;
        BigDecimal open = BigDecimal.ZERO;
        BigDecimal high = BigDecimal.ZERO;
        BigDecimal low = BigDecimal.ZERO;
        BigDecimal close = BigDecimal.ZERO;
        BigDecimal volume = BigDecimal.ZERO;
        for(int i = 0; i < series.size(); i ++ ) {
            Ohlcv ohlcv = series.get(i);
            if(i == 0) {
                type = ohlcv.getType();
                datetime = ohlcv.getDateTime();
                timezone = ohlcv.getTimeZone();
                open = ohlcv.getOpen();
                high = ohlcv.getHigh();
                low  = ohlcv.getLow();
                close = ohlcv.getClose();
                volume = ohlcv.getVolume();
            }else{
                datetime = ohlcv.getDateTime();
                if(ohlcv.getHigh().compareTo(high) > 0) {
                    high = ohlcv.getHigh();
                }
                if(ohlcv.getLow().compareTo(low ) < 0) {
                    low  = ohlcv.getLow();
                }
                close = ohlcv.getClose();
                volume = volume.add(ohlcv.getVolume());
            }
        }

        // return resampled ohlcvs
        return Ohlcv.builder()
                .type(type)
                .dateTime(datetime)
                .timeZone(timezone)
                .open(open)
                .high(high)
                .low(low )
                .close(close)
                .volume(volume)
                .build();
    }

    public static TradeAsset from(TradeAssetEntity assetEntity) {
        return TradeAsset.builder()
                .tradeId(assetEntity.getTradeId())
                .assetId(assetEntity.getAssetId())
                .dateTime(assetEntity.getDateTime())
                .previousClose(assetEntity.getPreviousClose())
                .open(assetEntity.getOpen())
                .close(assetEntity.getClose())
                .volume(assetEntity.getVolume())
                .message(assetEntity.getMessage())
                .context(assetEntity.getContext())
                .build();
    }

}