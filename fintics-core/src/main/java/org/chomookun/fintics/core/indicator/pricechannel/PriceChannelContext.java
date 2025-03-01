package org.chomookun.fintics.core.indicator.pricechannel;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class PriceChannelContext extends IndicatorContext {

    public static final PriceChannelContext DEFAULT = PriceChannelContext.of(20);

    private final int period;

    /**
     * Creates price channel context
     * @param period period
     * @return price channel context
     */
    public static PriceChannelContext of(int period) {
        return PriceChannelContext.builder()
                .period(period)
                .build();
    }

}
