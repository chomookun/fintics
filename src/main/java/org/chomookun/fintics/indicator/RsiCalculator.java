package org.chomookun.fintics.indicator;

import org.chomookun.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class RsiCalculator extends IndicatorCalculator<RsiContext, Rsi> {

    public RsiCalculator(RsiContext context) {
        super(context);
    }

    @Override
    public List<Rsi> calculate(List<Ohlcv> series) {
        // price changes
        List<BigDecimal> priceChanges = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            if(i == 0) {
                priceChanges.add(BigDecimal.ZERO);
                continue;
            }
            BigDecimal priceChange = series.get(i).getClose()
                    .subtract(series.get(i - 1).getClose());
            priceChanges.add(priceChange);
        }

        // gain/loss values
        List<BigDecimal> gains = new ArrayList<>();
        List<BigDecimal> losses = new ArrayList<>();
        for (BigDecimal priceChange : priceChanges) {
            if(priceChange.compareTo(BigDecimal.ZERO) > 0) {
                gains.add(priceChange);
                losses.add(BigDecimal.ZERO);
            }else if(priceChange.compareTo(BigDecimal.ZERO) < 0) {
                gains.add(BigDecimal.ZERO);
                losses.add(priceChange.multiply(BigDecimal.valueOf(-1)));
            }else{
                gains.add(BigDecimal.ZERO);
                losses.add(BigDecimal.ZERO);
            }
        }

        List<BigDecimal> rsiValues = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            // period data of gain/loss
            List<BigDecimal> periodGains = gains.subList(
                    Math.max(i - getContext().getPeriod() + 1,0),
                    i + 1
            );
            List<BigDecimal> periodLosses = losses.subList(
                    Math.max(i - getContext().getPeriod() + 1,0),
                    i + 1
            );

            // The average before period +1 is not an accurate period average, so it is set to neutral (50.00)
            if(i < getContext().getPeriod() + 1) {
                rsiValues.add(BigDecimal.valueOf(50.00));
                continue;
            }

            // average of gain/loss
            BigDecimal avgGain = getAverage(periodGains);
            BigDecimal avgLoss = getAverage(periodLosses);

            if(avgLoss.compareTo(BigDecimal.ZERO) == 0) {
                if(avgGain.compareTo(BigDecimal.ZERO) == 0) {
                    rsiValues.add(BigDecimal.ZERO);
                }else{
                    rsiValues.add(BigDecimal.valueOf(100.0));
                }
                continue;
            }

            // calculate relative Strength (RS)
            BigDecimal rs = avgLoss.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO
                    : avgGain.divide(avgLoss, MathContext.DECIMAL128)
                    .setScale(5, RoundingMode.HALF_UP);

            // calculate RSI
            BigDecimal rsiValue = rs
                    .divide(rs.add(BigDecimal.valueOf(1)), MathContext.DECIMAL32)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
            rsiValues.add(rsiValue);
        }

        // signals
        List<BigDecimal> signals = emas(rsiValues, getContext().getSignalPeriod(), getContext().getMathContext()).stream()
                .map(value -> value.setScale(2, RoundingMode.HALF_UP))
                .toList();

        // rsi
        List<Rsi> rsis = new ArrayList<>();
        for(int i = 0; i < rsiValues.size(); i ++ ) {
            Rsi rsi = Rsi.builder()
                    .dateTime(series.get(i).getDateTime())
                    .value(rsiValues.get(i))
                    .signal(signals.get(i))
                    .build();
            rsis.add(rsi);
        }
        return rsis;
    }

    private static BigDecimal getAverage(List<BigDecimal> values) {
        if(values.isEmpty()){
            return BigDecimal.ZERO;
        }
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), MathContext.DECIMAL32);
    }

}
