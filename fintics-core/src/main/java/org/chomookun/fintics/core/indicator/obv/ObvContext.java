package org.chomookun.fintics.core.indicator.obv;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.IndicatorContext;

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
