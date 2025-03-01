package org.chomookun.fintics.core.indicator.bolangerband;

import org.chomookun.fintics.core.indicator.IndicatorCalculator;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class BollingerBandCalculator extends IndicatorCalculator<BollingerBandContext, BollingerBand> {

    /**
     * Constructor
     * @param context bollinger band context
     */
    public BollingerBandCalculator(BollingerBandContext context) {
        super(context);
    }

    /**
     * Calculate Bollinger Bands
     * @param series series
     * @return bollinger bands
     */
    @Override
    public List<BollingerBand> calculate(List<Ohlcv> series) {
        int period = getContext().getPeriod();
        BigDecimal sdMultiplier = BigDecimal.valueOf(getContext().getSdMultiplier());
        MathContext mathContext = getContext().getMathContext();
        // close prices, smas, sds
        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClose)
                .toList();
        List<BigDecimal> smas = smas(closePrices, period, mathContext);
        List<BigDecimal> sds = sds(closePrices, period, mathContext);
        // bollinger bands
        List<BollingerBand> bollingerBands = new ArrayList<>();
        for (int i = 0; i < series.size(); i ++) {
            BigDecimal sd = sds.get(i);
            BigDecimal middle = smas.get(i);
            BigDecimal upper = middle.add(sd.multiply(sdMultiplier));
            BigDecimal lower = middle.subtract(sd.multiply(sdMultiplier));
            // width
            BigDecimal width = BigDecimal.ZERO;
            if (middle.compareTo(BigDecimal.ZERO) != 0) {
                width = upper.subtract(lower)
                        .divide(middle, getContext().getMathContext());
            }
            // percent B
            BigDecimal percentB = BigDecimal.ZERO;
            BigDecimal diffUpperLower = upper.subtract(lower);
            if (diffUpperLower.compareTo(BigDecimal.ZERO) != 0) {
                percentB = (closePrices.get(i).subtract(lower))
                        .divide(diffUpperLower, getContext().getMathContext())
                        .multiply(BigDecimal.valueOf(100));
            }
            BollingerBand bollingerBand = BollingerBand.builder()
                    .dateTime(series.get(i).getDateTime())
                    .middle(middle)
                    .upper(upper)
                    .lower(lower)
                    .width(width)
                    .percentB(percentB)
                    .build();
            bollingerBands.add(bollingerBand);
        }
        return bollingerBands;
    }

}
