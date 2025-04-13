package org.chomookun.fintics.web.api.v1.asset.dto;

import lombok.*;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OhlcvResponse {

    private String assetId;

    private Ohlcv.Type type;

    private LocalDateTime dateTime;

    private ZoneId timeZone;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal volume;

    private boolean interpolated;

    /**
     * creates ohlcv response
     * @param ohlcv ohlcv
     * @return ohlcv response
     */
    public static OhlcvResponse from(Ohlcv ohlcv) {
        return OhlcvResponse.builder()
                .assetId(ohlcv.getAssetId())
                .type(ohlcv.getType())
                .dateTime(ohlcv.getDateTime())
                .timeZone(ohlcv.getTimeZone())
                .open(ohlcv.getOpen())
                .high(ohlcv.getHigh())
                .low(ohlcv.getLow())
                .close(ohlcv.getClose())
                .volume(ohlcv.getVolume())
                .interpolated(ohlcv.isInterpolated())
                .build();
    }

}
