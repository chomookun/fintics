package org.chomookun.fintics.core.indicator.stochasticslow;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class StochasticSlowContext extends IndicatorContext {

    public static final StochasticSlowContext DEFAULT = StochasticSlowContext.of(14, 3, 3);

    private final int period;

    private final int periodK;

    private final int periodD;

    /**
     * Creates stochastic slow context
     * @param period period
     * @param periodK period K
     * @param periodD period D
     * @return stochastic slow context
     */
    public static StochasticSlowContext of(int period, int periodK, int periodD) {
        return StochasticSlowContext.builder()
                .period(period)
                .periodK(periodK)
                .periodD(periodD)
                .build();
    }

}
