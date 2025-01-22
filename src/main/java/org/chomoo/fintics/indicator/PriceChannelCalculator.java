package org.chomoo.fintics.indicator;

import org.chomoo.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PriceChannelCalculator extends IndicatorCalculator<PriceChannelContext, PriceChannel> {

    public PriceChannelCalculator(PriceChannelContext context) {
        super(context);
    }

    @Override
    public List<PriceChannel> calculate(List<Ohlcv> series) {
        PriceChannelContext context = getContext();
        MathContext mathContext = context.getMathContext();
        int period = context.getPeriod();

        // loop
        List<PriceChannel> priceChannels = new ArrayList<>();
        for (int i = 0; i < series.size(); i++) {
            // period series (except current tick. shift 1 tick)
            List<Ohlcv> periodSeries = series.subList(
                    Math.max(i - period , 0),
                    i
            );

            // upper
            BigDecimal upper = periodSeries.stream()
                    .map(Ohlcv::getHigh)
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            // lower
            BigDecimal lower = periodSeries.stream()
                    .map(Ohlcv::getLow)
                    .min(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            // middle
            BigDecimal middle = upper.add(lower)
                    .divide(new BigDecimal(2), mathContext);

            // PriceChannel 객체 생성
            PriceChannel priceChannel = PriceChannel.builder()
                    .dateTime(series.get(i).getDateTime())
                    .upper(upper)
                    .lower(lower)
                    .middle(middle)
                    .build();
            priceChannels.add(priceChannel);
        }

        // return
        return priceChannels;
    }

}
