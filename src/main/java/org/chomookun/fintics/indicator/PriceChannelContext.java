package org.chomookun.fintics.indicator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class PriceChannelContext extends IndicatorContext {

    public static final PriceChannelContext DEFAULT = PriceChannelContext.of(20);

    private final int period;

    public static PriceChannelContext of(int period) {
        return PriceChannelContext.builder()
                .period(period)
                .build();
    }

}
