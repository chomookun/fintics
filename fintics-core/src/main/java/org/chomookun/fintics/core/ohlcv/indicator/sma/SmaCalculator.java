package org.chomookun.fintics.core.ohlcv.indicator.sma;

import org.chomookun.fintics.core.ohlcv.indicator.IndicatorCalculator;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SmaCalculator extends IndicatorCalculator<SmaContext, Sma> {

    /**
     * Constructor
     * @param context sma context
     */
    public SmaCalculator(SmaContext context) {
        super(context);
    }

    /**
     * Calculates sma
     * @param series ohlcv series
     * @return sma series
     */
    @Override
    public List<Sma> calculate(List<Ohlcv> series) {
        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClose)
                .toList();
        List<BigDecimal> smaValues = smas(closePrices, getContext().getPeriod(), getContext().getMathContext());
        List<Sma> smas = new ArrayList<>();
        for (int i = 0; i < smaValues.size(); i ++ ) {
            smas.add(Sma.builder()
                    .dateTime(series.get(i).getDateTime())
                    .value(smaValues.get(i))
                    .build());
        }
        return smas;
    }

}
