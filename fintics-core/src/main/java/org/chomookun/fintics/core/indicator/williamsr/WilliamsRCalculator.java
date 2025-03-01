package org.chomookun.fintics.core.indicator.williamsr;

import org.chomookun.fintics.core.indicator.IndicatorCalculator;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class WilliamsRCalculator extends IndicatorCalculator<WilliamsRContext, WilliamsR> {

    /**
     * Constructor
     * @param context williams R context
     */
    public WilliamsRCalculator(WilliamsRContext context) {
        super(context);
    }

    /**
     * Calculate williams R
     * @param series ohlcv series
     * @return williams R series
     */
    @Override
    public List<WilliamsR> calculate(List<Ohlcv> series) {
        List<BigDecimal> williamsRValues = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            if (i < getContext().getPeriod() - 1) {
                williamsRValues.add(BigDecimal.valueOf(50.00)); // 중립값 설정
                continue;
            }
            // high price
            BigDecimal highestHigh = series.subList(i - getContext().getPeriod() + 1, i + 1).stream()
                    .map(Ohlcv::getHigh)
                    .max(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            // low price
            BigDecimal lowestLow = series.subList(i - getContext().getPeriod() + 1, i + 1).stream()
                    .map(Ohlcv::getLow)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
            // close price
            BigDecimal closePrice = series.get(i).getClose();
            // williams R
            BigDecimal williamsRValue = (highestHigh.equals(lowestLow))
                    ? BigDecimal.ZERO
                    : highestHigh.subtract(closePrice)
                    .divide(highestHigh.subtract(lowestLow), MathContext.DECIMAL128)
                    .multiply(BigDecimal.valueOf(-100))
                    .setScale(2, RoundingMode.HALF_UP);
            williamsRValues.add(williamsRValue);
        }
        // signals
        List<BigDecimal> signalValues = emas(williamsRValues, getContext().getSignalPeriod(), getContext().getMathContext())
                .stream()
                .map(value -> value.setScale(2, RoundingMode.HALF_UP))
                .toList();
        // williamsR
        List<WilliamsR> williamsRs = new ArrayList<>();
        for (int i = 0; i < williamsRValues.size(); i++) {
            WilliamsR williamsR = WilliamsR.builder()
                    .dateTime(series.get(i).getDateTime())
                    .value(williamsRValues.get(i))
                    .signal(signalValues.get(i))
                    .build();
            williamsRs.add(williamsR);
        }
        // return
        return williamsRs;
    }

}
