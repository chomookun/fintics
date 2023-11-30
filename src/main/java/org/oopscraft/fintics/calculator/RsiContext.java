package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class RsiContext extends CalculateContext {

    public static final RsiContext DEFAULT = RsiContext.of(14, 9);

    private final int period;

    private final int signalPeriod;

    public static RsiContext of(int period, int signalPeriod) {
        return RsiContext.builder()
                .period(period)
                .signalPeriod(signalPeriod)
                .build();
    }

}
