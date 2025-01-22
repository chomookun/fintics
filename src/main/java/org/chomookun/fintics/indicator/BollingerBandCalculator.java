package org.chomookun.fintics.indicator;

import org.chomookun.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class BollingerBandCalculator extends IndicatorCalculator<BollingerBandContext, BollingerBand> {

    public BollingerBandCalculator(BollingerBandContext context) {
        super(context);
    }

    @Override
    public List<BollingerBand> calculate(List<Ohlcv> series) {
        int period = getContext().getPeriod();
        BigDecimal sdMultiplier = BigDecimal.valueOf(getContext().getSdMultiplier());
        MathContext mathContext = getContext().getMathContext();

        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClose)
                .toList();
        List<BigDecimal> smas = smas(closePrices, period, mathContext);
        List<BigDecimal> sds = sds(closePrices, period, mathContext);

        List<BollingerBand> bollingerBands = new ArrayList<>();
        for (int i = 0; i < series.size(); i ++) {
            BigDecimal sd = sds.get(i);
            BigDecimal middle = smas.get(i);
            BigDecimal upper = middle.add(sd.multiply(sdMultiplier));
            BigDecimal lower = middle.subtract(sd.multiply(sdMultiplier));

            BigDecimal width = BigDecimal.ZERO;
            if (middle.compareTo(BigDecimal.ZERO) != 0) {
                width = upper.subtract(lower)
                        .divide(middle, getContext().getMathContext());
            }

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
