package org.chomookun.fintics.core.ohlcv.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.ohlcv.entity.OhlcvEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Ohlcv {

    private String assetId;

    private Type type;

    private LocalDateTime dateTime;

    private ZoneId timeZone;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal volume;

    private boolean interpolated;

    private boolean cached;

    public enum Type { MINUTE, DAILY }

    public static Ohlcv of(String assetId, Ohlcv.Type type, LocalDateTime dateTime, ZoneId timeZone, double open, double high, double low, double close, double volume) {
        return Ohlcv.builder()
                .assetId(assetId)
                .type(type)
                .dateTime(dateTime)
                .timeZone(timeZone)
                .open(BigDecimal.valueOf(open))
                .high(BigDecimal.valueOf(high))
                .low(BigDecimal.valueOf(low))
                .close(BigDecimal.valueOf(close))
                .volume(BigDecimal.valueOf(volume))
                .build();
    }

    public static Ohlcv from(OhlcvEntity ohlcvEntity) {
        return Ohlcv.builder()
                .assetId(ohlcvEntity.getAssetId())
                .type(ohlcvEntity.getType())
                .dateTime(ohlcvEntity.getDateTime())
                .timeZone(ohlcvEntity.getTimeZone())
                .open(ohlcvEntity.getOpen())
                .high(ohlcvEntity.getHigh())
                .low(ohlcvEntity.getLow())
                .close(ohlcvEntity.getClose())
                .volume(ohlcvEntity.getVolume())
                .interpolated(ohlcvEntity.isInterpolated())
                .build();
    }

}
