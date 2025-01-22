package org.chomookun.fintics.indicator;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.math.MathContext;

@SuperBuilder
public abstract class IndicatorContext {

    @Getter
    @Builder.Default
    private MathContext mathContext = MathContext.DECIMAL32;

}
