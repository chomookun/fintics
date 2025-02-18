package org.chomookun.fintics.core.indicator.chaikinoscillator;

import org.chomookun.fintics.core.indicator.IndicatorCalculator;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ChaikinOscillatorCalculator extends IndicatorCalculator<ChaikinOscillatorContext, ChaikinOscillator> {

    public ChaikinOscillatorCalculator(ChaikinOscillatorContext context) {
        super(context);
    }

    @Override
    public List<ChaikinOscillator> calculate(List<Ohlcv> series) {
        // ad values
        List<BigDecimal> adValues = new ArrayList<>();
        BigDecimal adValue = BigDecimal.ZERO;
        for (Ohlcv ohlcv : series) {
            BigDecimal closeLowDiff = ohlcv.getClose().subtract(ohlcv.getLow());
            BigDecimal highCloseDiff = ohlcv.getHigh().subtract(ohlcv.getClose());
            BigDecimal highLowDiff = ohlcv.getHigh().subtract(ohlcv.getLow());
            BigDecimal mfm;
            if (highLowDiff.compareTo(BigDecimal.ZERO) == 0) {
                mfm = BigDecimal.ZERO;
            } else {
                mfm = closeLowDiff.subtract(highCloseDiff)
                        .divide(highLowDiff, getContext().getMathContext());
            }
            BigDecimal mfVolume = mfm.multiply(ohlcv.getVolume());
            adValue = adValue.add(mfVolume);
            adValues.add(new BigDecimal(adValue.unscaledValue(), adValue.scale()));
        }

        // values
        List<BigDecimal> shortEmas = emas(adValues, getContext().getShortPeriod(), getContext().getMathContext());
        List<BigDecimal> longEmas = emas(adValues, getContext().getLongPeriod(), getContext().getMathContext());
        List<BigDecimal> values = new ArrayList<>();
        for(int i = 0; i < shortEmas.size(); i ++) {
            BigDecimal value = shortEmas.get(i).subtract(longEmas.get(i));
            values.add(value);
        }

        // signal
        List<BigDecimal> signals = emas(values, getContext().getSignalPeriod(), getContext().getMathContext());

        // chaikin's oscillator
        List<ChaikinOscillator> cos = new ArrayList<>();
        for(int i = 0; i < values.size(); i ++) {
            ChaikinOscillator co = ChaikinOscillator.builder()
                    .dateTime(series.get(i).getDateTime())
                    .value(values.get(i).setScale(2, RoundingMode.HALF_UP))
                    .signal(signals.get(i).setScale(2, RoundingMode.HALF_UP))
                    .build();
            cos.add(co);
        }
        return cos;
    }

}
