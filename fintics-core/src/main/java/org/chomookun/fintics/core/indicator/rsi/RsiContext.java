package org.chomookun.fintics.core.indicator.rsi;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class RsiContext extends IndicatorContext {

    public static final RsiContext DEFAULT = RsiContext.of(14, 9);

    private final int period;

    private final int signalPeriod;

    /**
     * Creates rsi context
     * @param period period
     * @param signalPeriod signal period
     * @return rsi context
     */
    public static RsiContext of(int period, int signalPeriod) {
        return RsiContext.builder()
                .period(period)
                .signalPeriod(signalPeriod)
                .build();
    }

}
