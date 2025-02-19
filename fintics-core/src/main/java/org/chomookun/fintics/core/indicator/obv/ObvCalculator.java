package org.chomookun.fintics.core.indicator.obv;

import org.chomookun.fintics.core.indicator.IndicatorCalculator;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class ObvCalculator extends IndicatorCalculator<ObvContext, Obv> {

    public ObvCalculator(ObvContext context) {
        super(context);
    }

    @Override
    public List<Obv> calculate(List<Ohlcv> series) {
        // values
        List<BigDecimal> obvValues = new ArrayList<>();
        obvValues.add(BigDecimal.ZERO);
        BigDecimal obvValue = BigDecimal.ZERO;
        for(int i = 1, size = series.size(); i < size; i ++) {
            Ohlcv prevOhlcv = series.get(i-1);
            Ohlcv currentOhlcv = series.get(i);
            BigDecimal volume = currentOhlcv.getVolume();
            BigDecimal priceDiff = currentOhlcv.getClose().subtract(prevOhlcv.getClose());
            if(priceDiff.compareTo(BigDecimal.ZERO) > 0) {
                obvValue = obvValue.add(volume);
            }else if(priceDiff.compareTo(BigDecimal.ZERO) < 0) {
                obvValue = obvValue.subtract(volume);
            }
            obvValues.add(new BigDecimal(obvValue.unscaledValue(), obvValue.scale()));
        }

        // signal
        List<BigDecimal> signals = emas(obvValues, getContext().getSignalPeriod(), getContext().getMathContext()).stream()
                .map(value -> value.setScale(0, RoundingMode.HALF_UP))
                .toList();

        // obv
        List<Obv> obvs = new ArrayList<>();
        for(int i = 0; i < obvValues.size(); i ++) {
            Obv obv = Obv.builder()
                    .dateTime(series.get(i).getDateTime())
                    .value(obvValues.get(i).setScale(0, RoundingMode.HALF_UP))
                    .signal(signals.get(i).setScale(0, RoundingMode.HALF_UP))
                    .build();
            obvs.add(obv);
        }
        return obvs;
    }


}
