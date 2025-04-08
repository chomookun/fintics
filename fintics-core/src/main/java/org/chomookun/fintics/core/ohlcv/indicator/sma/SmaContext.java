package org.chomookun.fintics.core.ohlcv.indicator.sma;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.ohlcv.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class SmaContext extends IndicatorContext {

    public static final SmaContext DEFAULT = SmaContext.of(20);

    private final int period;

    /**
     * Creates sma context
     * @param period period
     * @return sma context
     */
    public static SmaContext of(int period) {
        return SmaContext.builder()
                .period(period)
                .build();
    }

}
