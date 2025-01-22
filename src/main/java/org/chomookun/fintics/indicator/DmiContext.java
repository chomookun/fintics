package org.chomookun.fintics.indicator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class DmiContext extends IndicatorContext {

    public static final DmiContext DEFAULT = DmiContext.of(14);

    private final int period;

    public static DmiContext of(int period) {
        return DmiContext.builder()
                .period(period)
                .build();
    }

}
