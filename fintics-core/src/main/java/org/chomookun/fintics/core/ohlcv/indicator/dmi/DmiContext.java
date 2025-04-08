package org.chomookun.fintics.core.ohlcv.indicator.dmi;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.ohlcv.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class DmiContext extends IndicatorContext {

    public static final DmiContext DEFAULT = DmiContext.of(14);

    private final int period;

    /**
     * Creates dmi context
     * @param period period
     * @return dmi context
     */
    public static DmiContext of(int period) {
        return DmiContext.builder()
                .period(period)
                .build();
    }

}
