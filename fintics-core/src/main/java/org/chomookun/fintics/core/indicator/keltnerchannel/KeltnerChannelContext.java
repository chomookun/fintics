package org.chomookun.fintics.core.indicator.keltnerchannel;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class KeltnerChannelContext extends IndicatorContext {

    public static final KeltnerChannelContext DEFAULT = KeltnerChannelContext.of(20,10, 1.5);

    private final int period;

    private final int atrPeriod;

    private final double multiplier;

    /**
     * Creates keltner channel context
     * @param period period
     * @param atrPeriod atr period
     * @param multiplier multiplier
     * @return keltner channel context
     */
    public static KeltnerChannelContext of(int period, int atrPeriod, double multiplier) {
        return KeltnerChannelContext.builder()
                .period(period)
                .atrPeriod(atrPeriod)
                .multiplier(multiplier)
                .build();
    }

}
