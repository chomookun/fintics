package org.chomookun.fintics.core.indicator.atr;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class AtrContext extends IndicatorContext {

    public static final AtrContext DEFAULT = AtrContext.of(14, 9);

    private final int period;

    private final int signalPeriod;

    /**
     * Creates atr context
     * @param period period
     * @param signalPeriod signal period
     * @return atr context
     */
    public static AtrContext of(int period, int signalPeriod) {
        return AtrContext.builder()
                .period(period)
                .signalPeriod(signalPeriod)
                .build();
    }

}
