package org.chomookun.fintics.core.ohlcv.indicator.macd;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.ohlcv.indicator.IndicatorContext;

@SuperBuilder
@Getter
public class MacdContext extends IndicatorContext {

    public static final MacdContext DEFAULT = MacdContext.of(12, 26, 9);

    private final int shortPeriod;

    private final int longPeriod;

    private final int signalPeriod;

    /**
     * Creates macd context
     * @param shortPeriod short period
     * @param longPeriod long period
     * @param signalPeriod signal period
     * @return macd context
     */
    public static MacdContext of(int shortPeriod, int longPeriod, int signalPeriod) {
        return MacdContext.builder()
                .shortPeriod(shortPeriod)
                .longPeriod(longPeriod)
                .signalPeriod(signalPeriod)
                .build();
    }

}
