package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class OhlcvResponse {

    private Ohlcv.Type type;

    private LocalDateTime dateTime;

    private ZoneId timeZone;

    private BigDecimal open;

    private BigDecimal high;

    private BigDecimal low;

    private BigDecimal close;

    private BigDecimal volume;

    /**
     * creates ohlcv response
     * @param ohlcv ohlcv
     * @return ohlcv response
     */
    public static OhlcvResponse from(Ohlcv ohlcv) {
        return OhlcvResponse.builder()
                .dateTime(ohlcv.getDateTime())
                .timeZone(ohlcv.getTimeZone())
                .type(ohlcv.getType())
                .open(ohlcv.getOpen())
                .high(ohlcv.getHigh())
                .low(ohlcv.getLow())
                .close(ohlcv.getClose())
                .volume(ohlcv.getVolume())
                .build();
    }

}
