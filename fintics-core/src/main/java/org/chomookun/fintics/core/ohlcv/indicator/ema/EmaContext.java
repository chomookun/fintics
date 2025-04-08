package org.chomookun.fintics.core.ohlcv.indicator.ema;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.ohlcv.indicator.IndicatorContext;

import java.math.MathContext;

@SuperBuilder
@Getter
public class EmaContext extends IndicatorContext {

    public static final EmaContext DEFAULT = EmaContext.of(20);

    private final int period;

    /**
     * Creates ema context
     * @param period period
     * @return ema context
     */
    public static EmaContext of(int period) {
        return EmaContext.builder()
                .period(period)
                .build();
    }

    /**
     * Creates ema context with math context
     * @param period period
     * @param mathContext math context
     * @return ema context
     */
    public static EmaContext of(int period, MathContext mathContext) {
        return EmaContext.builder()
                .period(period)
                .mathContext(mathContext)
                .build();
    }

}
