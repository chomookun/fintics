package org.chomoo.fintics.indicator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class ObvContext extends IndicatorContext {

    public static final ObvContext DEFAULT = ObvContext.of(14, 9);

    private final int period;

    private final int signalPeriod;

    public static ObvContext of(int period, int signalPeriod) {
        return ObvContext.builder()
                .period(period)
                .signalPeriod(signalPeriod)
                .build();
    }

}
