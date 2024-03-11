package org.oopscraft.fintics.calculator;

import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.oopscraft.fintics.model.Ohlcv;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;

public class CciCalculator extends Calculator<CciContext, Cci> {

    private static final BigDecimal SCALING_FACTOR = BigDecimal.valueOf(0.015);

    public CciCalculator(CciContext context) {
        super(context);
    }

    @Override
    public List<Cci> calculate(List<Ohlcv> series) {
        int period = getContext().getPeriod();
        int signalPeriod = getContext().getSignalPeriod();
        MathContext mathContext = getContext().getMathContext();

        // typical price
        List<BigDecimal> typicalPrices = series.stream()
                .map(ohlcv -> ohlcv.getHighPrice()
                        .add(ohlcv.getLowPrice())
                        .add(ohlcv.getClosePrice())
                        .divide(BigDecimal.valueOf(3), mathContext))
                .toList();

        List<BigDecimal> smas = smas(typicalPrices, period, mathContext);
        List<BigDecimal> stds = ads(typicalPrices, period, mathContext);

        // calculate cci value
        List<BigDecimal> cciValues = new ArrayList<>();
        for (int i = 0; i < series.size(); i ++) {
            BigDecimal typicalPrice = typicalPrices.get(i);
            BigDecimal sma = smas.get(i);
            BigDecimal std = stds.get(i);
            if (std.compareTo(BigDecimal.ZERO) == 0) {
                cciValues.add(BigDecimal.ZERO);
                continue;
            }
            BigDecimal cciValue = typicalPrice.subtract(sma)
                    .divide(std.multiply(SCALING_FACTOR), mathContext);
            cciValues.add(cciValue);
        }

        // signals
        List<BigDecimal> cciSignals = emas(cciValues, signalPeriod, mathContext);

        // cci
        List<Cci> ccis = new ArrayList<>();
        for (int i = 0; i < series.size(); i ++ ) {
            ccis.add(Cci.builder()
                    .dateTime(series.get(i).getDateTime())
                    .value(cciValues.get(i))
                    .signal(cciSignals.get(i))
                    .build());
        }
        return ccis;
    }

}
