package org.chomookun.fintics.core.ohlcv.indicator.ema;

import org.chomookun.fintics.core.ohlcv.indicator.IndicatorCalculator;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EmaCalculator extends IndicatorCalculator<EmaContext, Ema> {

    /**
     * Constructor
     * @param context
     */
    public EmaCalculator(EmaContext context) {
        super(context);
    }

    /**
     * Calculate EMA
     * @param series ohlcv series
     * @return ema series
     */
    @Override
    public List<Ema> calculate(List<Ohlcv> series) {
        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClose)
                .toList();
        List<BigDecimal> emaValues = emas(closePrices, getContext().getPeriod(), getContext().getMathContext());
        List<Ema> emas = new ArrayList<>();
        for (int i = 0; i < emaValues.size(); i ++ ) {
            emas.add(Ema.builder()
                    .dateTime(series.get(i).getDateTime())
                    .value(emaValues.get(i))
                    .build());
        }
        return emas;
    }

}
