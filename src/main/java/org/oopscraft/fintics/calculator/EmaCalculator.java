package org.oopscraft.fintics.calculator;

import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class EmaCalculator extends Calculator<EmaContext, Ema> {

    public EmaCalculator(EmaContext context) {
        super(context);
    }

    @Override
    public List<Ema> calculate(List<Ohlcv> series) {
        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClosePrice)
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
