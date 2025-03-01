package org.chomookun.fintics.core.indicator.bolangerband;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class BollingerBandContext extends IndicatorContext {

    public static final BollingerBandContext DEFAULT = BollingerBandContext.of(20, 2);

    private final int period;

    private final int sdMultiplier;

    /**
     * Creates bollinger band context
     * @param period period
     * @param sdMultiplier sd multiplier
     * @return bollinger band context
     */
    public static BollingerBandContext of(int period, int sdMultiplier) {
        return BollingerBandContext.builder()
                .period(period)
                .sdMultiplier(sdMultiplier)
                .build();
    }

}
