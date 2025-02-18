package org.chomookun.fintics.core.indicator;

import lombok.Getter;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public abstract class IndicatorCalculator<C extends IndicatorContext, R extends Indicator> {

    @Getter
    private final C context;

    public IndicatorCalculator(C context) {
        this.context = context;
    }

    /**
     * calculate abstract method
     * @param series series
     * @return list of indicator
     */
    public abstract List<R> calculate(List<Ohlcv> series);

    /**
     * calculates exponential moving average
     * @param series series
     * @param period period
     * @param mathContext math context
     * @return list of exponential moving average
     */
    public static List<BigDecimal> emas(List<BigDecimal> series, int period, MathContext mathContext) {
        List<BigDecimal> emas = new ArrayList<>();
        BigDecimal multiplier = BigDecimal.valueOf(2.0)
                .divide(BigDecimal.valueOf(period + 1), mathContext);
        BigDecimal ema = series.isEmpty() ? BigDecimal.ZERO : series.get(0);
        emas.add(ema);
        for (int i = 1; i < series.size(); i++) {
            BigDecimal emaDiff = series.get(i).subtract(ema);
            ema = emaDiff
                    .multiply(multiplier, mathContext)
                    .add(ema);
            emas.add(ema);
        }
        return emas;
    }

    /**
     * calculates simple moving average
     * @param series series
     * @param period period
     * @param mathContext math context
     * @return list of simple moving average
     */
    public static List<BigDecimal> smas(List<BigDecimal> series, int period, MathContext mathContext) {
        List<BigDecimal> smas = new ArrayList<>();
        for(int i = 0; i < series.size(); i ++) {
            List<BigDecimal> periodSeries = series.subList(
                    Math.max(i - period + 1, 0),
                    i + 1
            );

            BigDecimal sum = BigDecimal.ZERO;
            for(BigDecimal value : periodSeries) {
                sum = sum.add(value);
            }

            BigDecimal sma = sum.divide(BigDecimal.valueOf(periodSeries.size()), mathContext);
            smas.add(sma);
        }
        return smas;
    }

    /**
     * calculates standard deviation
     * @param series series
     * @param period period
     * @param mathContext math context
     * @return list of standard deviation
     */
    public static List<BigDecimal> sds(List<BigDecimal> series, int period, MathContext mathContext) {
        List<BigDecimal> stds = new ArrayList<>();
        for (int i = 0; i < series.size(); i ++) {
            List<BigDecimal> periodSeries = series.subList(
                    Math.max(i - period + 1, 0),
                    i + 1
            );
            BigDecimal mean = periodSeries.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(BigDecimal.valueOf(periodSeries.size()), mathContext);
            BigDecimal sumSquaredDeviations = periodSeries.stream()
                    .map(x -> x.subtract(mean).pow(2))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal variance = sumSquaredDeviations
                    .divide(BigDecimal.valueOf(periodSeries.size()), mathContext);
            BigDecimal std = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));
            stds.add(std);
        }
        return stds;
    }

    /**
     * calculates absolute deviation
     * @param series series
     * @param period period
     * @param mathContext math context
     * @return list of absolute deviation
     */
    public static List<BigDecimal> ads(List<BigDecimal> series, int period, MathContext mathContext) {
        List<BigDecimal> ads = new ArrayList<>();
        for (int i = 0; i < series.size(); i ++) {
            List<BigDecimal> periodSeries = series.subList(
                    Math.max(i - period + 1, 0),
                    i + 1
            );
            BigDecimal sum = periodSeries.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal mean = sum.divide(BigDecimal.valueOf(periodSeries.size()), mathContext);
            BigDecimal sumOfAbsoluteDifferences = periodSeries.stream()
                    .map(value -> value.subtract(mean).abs())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal ad = sumOfAbsoluteDifferences.divide(BigDecimal.valueOf(periodSeries.size()), mathContext);
            ads.add(ad);
        }
        return ads;
    }

}
