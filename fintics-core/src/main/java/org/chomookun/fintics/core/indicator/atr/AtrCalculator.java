package org.chomookun.fintics.core.indicator.atr;

import org.chomookun.fintics.core.indicator.IndicatorCalculator;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class AtrCalculator extends IndicatorCalculator<AtrContext, Atr> {

    public AtrCalculator(AtrContext context) {
        super(context);
    }

    @Override
    public List<Atr> calculate(List<Ohlcv> series) {
        // tr
        List<BigDecimal> trs = new ArrayList<>();
        for(int i = 0; i < series.size(); i ++ ) {
            BigDecimal high = series.get(i).getHigh();
            BigDecimal low = series.get(i).getLow();
            BigDecimal previousClose = series.get(Math.max(i-1,0)).getClose();
            BigDecimal hl = high.subtract(low);
            BigDecimal hc = high.subtract(previousClose);
            BigDecimal cl = previousClose.subtract(low);
            BigDecimal tr = hl.abs().max(hc.abs()).max(cl.abs());
            trs.add(tr);
        }

        // average tr, signal
        List<BigDecimal> values = smas(trs, getContext().getPeriod(), getContext().getMathContext());
        List<BigDecimal> signals = emas(values, getContext().getSignalPeriod(), getContext().getMathContext());

        // atr list
        List<Atr> atrs = new ArrayList<>();
        for(int i = 0; i < series.size(); i ++) {
            atrs.add(Atr.builder()
                    .dateTime(series.get(i).getDateTime())
                    .value(values.get(i))
                    .signal(signals.get(i))
                    .build());
        }

        // return
        return atrs;
    }

}
