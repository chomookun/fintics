package org.oopscraft.fintics.calculator;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class SmaCalculator {

    private final List<BigDecimal> series;

    private final int period;

    private final MathContext mathContext;

    public static SmaCalculator of(List<BigDecimal> series, int period) {
        return of(series, period, new MathContext(4, RoundingMode.HALF_UP));
    }

    public static SmaCalculator of(List<BigDecimal> series, int period, MathContext mathContext) {
        return new SmaCalculator(series, period, mathContext);
    }

    public SmaCalculator(List<BigDecimal> series, int period, MathContext mathContext) {
        this.series = series;
        this.period = period;
        this.mathContext = mathContext;
    }

    public List<BigDecimal> calculate() {
        List<BigDecimal> smas = new ArrayList<>();
        for(int i = 0; i < series.size(); i ++) {
            List<BigDecimal> perioidSeries = series.subList(
                Math.max(i - period + 1, 0),
                i + 1
            );

            BigDecimal sum = BigDecimal.ZERO;
            for(BigDecimal value : perioidSeries) {
                sum = sum.add(value);
            }

            BigDecimal sma = sum.divide(BigDecimal.valueOf(perioidSeries.size()), mathContext);
            smas.add(sma);
        }

        return smas;
    }

}
