package org.chomookun.fintics.core.ohlcv.indicator.williamsr;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.ohlcv.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class WilliamsRContext extends IndicatorContext {

    public static final WilliamsRContext DEFAULT = WilliamsRContext.of(14, 3);

    private final int period;

    private final int signalPeriod;

    /**
     * Creates williams r context
     * @param period period
     * @param signalPeriod signal period
     * @return williams r context
     */
    public static WilliamsRContext of(int period, int signalPeriod) {
        return WilliamsRContext.builder()
                .period(period)
                .signalPeriod(signalPeriod)
                .build();
    }

}
