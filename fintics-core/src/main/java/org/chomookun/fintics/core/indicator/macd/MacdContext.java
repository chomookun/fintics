package org.chomookun.fintics.core.indicator.macd;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class MacdContext extends IndicatorContext {

    public static final MacdContext DEFAULT = MacdContext.of(12, 26, 9);

    private final int shortPeriod;

    private final int longPeriod;

    private final int signalPeriod;

    public static MacdContext of(int shortPeriod, int longPeriod, int signalPeriod) {
        return MacdContext.builder()
                .shortPeriod(shortPeriod)
                .longPeriod(longPeriod)
                .signalPeriod(signalPeriod)
                .build();
    }

}
