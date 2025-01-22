package org.chomookun.fintics.indicator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class WilliamsRContext extends IndicatorContext {

    public static final WilliamsRContext DEFAULT = WilliamsRContext.of(14, 3);

    private final int period;

    private final int signalPeriod;

    public static WilliamsRContext of(int period, int signalPeriod) {
        return WilliamsRContext.builder()
                .period(period)
                .signalPeriod(signalPeriod)
                .build();
    }

}
