package org.chomookun.fintics.core.indicator.macd;

import org.chomookun.fintics.core.indicator.IndicatorCalculator;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class MacdCalculator extends IndicatorCalculator<MacdContext, Macd> {

    public MacdCalculator(MacdContext context) {
        super(context);
    }

    @Override
    public List<Macd> calculate(List<Ohlcv> series) {
        List<BigDecimal> closePrices = series.stream()
                .map(Ohlcv::getClose)
                .toList();

        List<BigDecimal> values = new ArrayList<>();
        List<BigDecimal> oscillators = new ArrayList<>();

        // Calculate MACD line
        List<BigDecimal> shortTermEmas = emas(closePrices, getContext().getShortPeriod(), getContext().getMathContext());
        List<BigDecimal> longTermEmas = emas(closePrices, getContext().getLongPeriod(), getContext().getMathContext());

        for (int i = 0; i < longTermEmas.size(); i++) {
            BigDecimal value = shortTermEmas.get(i).subtract(longTermEmas.get(i));
            values.add(value);
        }

        // ine using MACD line
        List<BigDecimal> signals = emas(values, getContext().getSignalPeriod(), getContext().getMathContext());

        // oscillator
        for (int i = 0; i < values.size(); i++) {
            BigDecimal oscillator = values.get(i).subtract(signals.get(i));
            oscillators.add(oscillator);
        }

        // return macds
        List<Macd> macds = new ArrayList<>();
        for(int i = 0; i < values.size(); i ++ ) {
            Macd macd = Macd.builder()
                    .dateTime(series.get(i).getDateTime())
                    .value(values.get(i))
                    .signal(signals.get(i))
                    .oscillator(oscillators.get(i))
                    .build();
            macds.add(macd);
        }
        return macds;
    }

}
