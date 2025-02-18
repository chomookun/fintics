package org.chomookun.fintics.core.indicator.sma;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class SmaContext extends IndicatorContext {

    public static final SmaContext DEFAULT = SmaContext.of(20);

    private final int period;

    public static SmaContext of(int period) {
        return SmaContext.builder()
                .period(period)
                .build();
    }

}
