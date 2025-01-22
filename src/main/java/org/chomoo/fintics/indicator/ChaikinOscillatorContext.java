package org.chomoo.fintics.indicator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class ChaikinOscillatorContext extends IndicatorContext {

    public static final ChaikinOscillatorContext DEFAULT = ChaikinOscillatorContext.of(3, 10, 9);

    private final int shortPeriod;

    private final int longPeriod;

    private final int signalPeriod;

    public static ChaikinOscillatorContext of(int shortPeriod, int longPeriod, int signalPeriod) {
        return ChaikinOscillatorContext.builder()
                .shortPeriod(shortPeriod)
                .longPeriod(longPeriod)
                .signalPeriod(signalPeriod)
                .build();
    }

}
