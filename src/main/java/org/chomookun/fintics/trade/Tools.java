package org.chomookun.fintics.trade;

import com.mitchtalmadge.asciidata.graph.ASCIIGraph;
import org.chomookun.fintics.indicator.IndicatorContext;
import org.chomookun.fintics.indicator.Indicator;
import org.chomookun.fintics.indicator.IndicatorCalculator;
import org.chomookun.fintics.indicator.IndicatorCalculatorFactory;
import org.chomookun.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Tools for strategy script
 */
public class Tools {

    /**
     * calculate technical indicator
     * @param ohlcvs OHLCV data (time descending)
     * @param context calculator context
     * @param <C> calculator context type
     * @param <R> return type
     * @return technical indicator results
     */
    public static <C extends IndicatorContext, R extends Indicator> List<R> indicators(List<Ohlcv> ohlcvs, C context) {
        // series
        List<Ohlcv> series = new ArrayList<>(ohlcvs);
        Collections.reverse(series);

        // calculate
        IndicatorCalculator<C,R> calculator = IndicatorCalculatorFactory.getIndicator(context);
        List<R> calculateResults =  calculator.calculate(series);

        // reverse and return
        Collections.reverse(calculateResults);
        return calculateResults;
    }

    /**
     * calculates percentage of change at each data point
     * @param values data points (time descending)
     * @return percentage of change
     */
    public static List<BigDecimal> pctChanges(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return new ArrayList<>();
        }
        List<BigDecimal> series = new ArrayList<>(values);
        Collections.reverse(series);

        List<BigDecimal> pctChanges = new ArrayList<>();
        pctChanges.add(BigDecimal.ZERO);
        for (int i = 1; i < series.size(); i++) {
            BigDecimal current = series.get(i);
            BigDecimal previous = series.get(i - 1);
            if(previous.compareTo(BigDecimal.ZERO) == 0) {
                if(current.compareTo(BigDecimal.ZERO) == 0) {
                    pctChanges.add(BigDecimal.ZERO);
                }else{
                    pctChanges.add(BigDecimal.valueOf(100));
                }
                continue;
            }
            BigDecimal pctChange = current.subtract(previous)
                    .divide(previous, MathContext.DECIMAL32)
                    .multiply(BigDecimal.valueOf(100));
            pctChanges.add(pctChange);
        }

        Collections.reverse(pctChanges);
        return pctChanges;
    }

    /**
     * calculate sum of all percentage of change
     * @param values each data point (time descending)
     * @return sum of all percentage of change
     */
    public static BigDecimal pctChange(List<BigDecimal> values) {
        List<BigDecimal> pctChanges = pctChanges(values);
        return sum(pctChanges);
    }

    /**
     * calculates z-score each element
     * @param values data point values (time descending)
     * @return z-score list at each data point
     */
    public static List<BigDecimal> zScores(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return new ArrayList<>();
        }
        List<BigDecimal> series = new ArrayList<>(values);
        Collections.reverse(series);

        BigDecimal mean = mean(series);
        List<BigDecimal> stds = IndicatorCalculator.sds(series, series.size(), MathContext.DECIMAL32);
        BigDecimal std = stds.get(stds.size() - 1);

        List<BigDecimal> zScores = new ArrayList<>();
        for(BigDecimal value : series) {
            if(std.compareTo(BigDecimal.ZERO) == 0) {
                zScores.add(BigDecimal.ZERO);
                continue;
            }
            BigDecimal zScore = value
                    .subtract(mean)
                    .divide(std, MathContext.DECIMAL32);
            zScores.add(zScore);
        }

        Collections.reverse(zScores);
        return zScores;
    }

    /**
     * get current(first) z-score
     * @param values data point values (time descending)
     * @return current z-score(first)
     */
    public static BigDecimal zScore(List<BigDecimal> values) {
        List<BigDecimal> zScores = zScores(values);
        return zScores.get(0);
    }

    /**
     * calculates sum value
     * @param values data points (time descending)
     * @return sum of all data points values
     */
    public static BigDecimal sum(List<BigDecimal> values) {
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * calculates mean(average) values
     * @param values data points (time descending)
     * @return mean(average) value
     */
    public static BigDecimal mean(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> series = new ArrayList<>(values);
        return series.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(series.size()), MathContext.DECIMAL32);
    }

    /**
     * calculates min value
     * @param values data points (time descending)
     * @return min value
     */
    public static BigDecimal min(List<BigDecimal> values) {
        return values.stream()
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * calculates max value
     * @param values data point (time descending)
     * @return max value
     */
    public static BigDecimal max(List<BigDecimal> values) {
        return values.stream()
                .max(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    /**
     * calculates median value
     * @param values data points (time descending)
     * @return median value
     */
    public static BigDecimal median(List<BigDecimal> values) {
        if(values.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<BigDecimal> sortedValues = new ArrayList<>(values);
        Collections.sort(sortedValues);
        int size = sortedValues.size();
        if (size % 2 == 1) {
            return sortedValues.get(size / 2);
        } else {
            BigDecimal leftMiddle = sortedValues.get(size / 2 - 1);
            BigDecimal rightMiddle = sortedValues.get(size / 2);
            return leftMiddle.add(rightMiddle)
                    .divide(BigDecimal.valueOf(2), MathContext.DECIMAL32);
        }
    }

    /**
     * check values are crossed
     * @param values1 values 1
     * @param values2 values 2
     * @return is value is cross
     */
    public static Boolean isCross(List<BigDecimal> values1, List<BigDecimal> values2) {
        BigDecimal minValues1 = min(values1);
        BigDecimal maxValues1 = max(values1);
        BigDecimal minValues2 = min(values2);
        BigDecimal maxValues2 = max(values2);
        return maxValues1.compareTo(minValues2) >= 0 || maxValues2.compareTo(minValues1) >= 0;
    }

    /**
     * check elements is ascending
     * @param values values
     * @return result
     */
    public static Boolean isAscending(List<BigDecimal> values) {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i).compareTo(values.get(i - 1)) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * check element is descending
     * @param values values
     * @return result
     */
    public static Boolean isDescending(List<BigDecimal> values) {
        for (int i = 1; i < values.size(); i++) {
            if (values.get(i).compareTo(values.get(i - 1)) > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * creates simple ascii line chart
     * @param title chart title
     * @param values data point
     * @return ascii chart string
     */
    public static Supplier<String> graph(String title, List<BigDecimal> values) {
        return new Supplier<>() {
            @Override
            public String get() {
                // make series
                List<BigDecimal> series = new ArrayList<>(values);
                Collections.reverse(series);

                // data to double array
                double[] doubles = series.stream()
                        .mapToDouble(BigDecimal::doubleValue)
                        .toArray();

                // create ascii graph
                String graph = ASCIIGraph.fromSeries(doubles).withNumRows(10).plot();
                return String.format("\n%s\n%s", title, graph);
            }

            @Override
            public String toString() {
                return get();
            }
        };
    }

    /**
     * creates simple ascii table
     * @param title table title
     * @param values values
     * @return ascii table string
     */
    public static Supplier<String> table(String title, List<BigDecimal> values) {
        return new Supplier<>() {
            @Override
            public String get() {
                // TODO
                return null;
            }

            @Override
            public String toString() {
                return get();
            }
        };
    }

}
