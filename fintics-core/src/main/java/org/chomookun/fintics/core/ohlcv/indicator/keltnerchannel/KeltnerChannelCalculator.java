package org.chomookun.fintics.core.ohlcv.indicator.keltnerchannel;

import org.chomookun.fintics.core.ohlcv.indicator.IndicatorCalculator;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class KeltnerChannelCalculator extends IndicatorCalculator<KeltnerChannelContext, KeltnerChannel> {

    /**
     * Constructor
     * @param context
     */
    public KeltnerChannelCalculator(KeltnerChannelContext context) {
        super(context);
    }

    /**
     * Calculate Keltner Channel
     * @param series ohlcv series
     * @return keltner channel series
     */
    @Override
    public List<KeltnerChannel> calculate(List<Ohlcv> series) {
        KeltnerChannelContext context = getContext();
        MathContext mathContext = context.getMathContext();
        int period = context.getPeriod();
        int atrPeriod = context.getAtrPeriod();
        double multiplier = context.getMultiplier();
        // closes
        List<BigDecimal> closes = series.stream()
                .map(Ohlcv::getClose)
                .toList();
        // ema
        List<BigDecimal> emas = emas(closes, period, mathContext);
        // atr
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
        List<BigDecimal> atrs = emas(trs, atrPeriod, mathContext);
        // keltner channel
        List<KeltnerChannel> keltnerChannels = new ArrayList<>();
        for (int i = context.getAtrPeriod(); i < series.size(); i++) {
            BigDecimal center = emas.get(i);
            BigDecimal upper = emas.get(i).add(atrs.get(i).multiply(BigDecimal.valueOf(multiplier)));
            BigDecimal lower = emas.get(i).subtract(atrs.get(i).multiply(BigDecimal.valueOf(multiplier)));
            KeltnerChannel keltnerChannel = KeltnerChannel.builder()
                    .center(center)
                    .upper(upper)
                    .lower(lower)
                    .build();
            keltnerChannels.add(keltnerChannel);
        }
        // return
        return keltnerChannels;
    }

}
