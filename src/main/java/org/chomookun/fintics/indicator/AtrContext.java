package org.chomookun.fintics.indicator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class AtrContext extends IndicatorContext {

    public static final AtrContext DEFAULT = AtrContext.of(14, 9);

    private final int period;

    private final int signalPeriod;

    public static AtrContext of(int period, int signalPeriod) {
        return AtrContext.builder()
                .period(period)
                .signalPeriod(signalPeriod)
                .build();
    }

}
