package org.chomookun.fintics.core.ohlcv.indicator;

import lombok.Getter;
import org.chomookun.fintics.core.ohlcv.indicator.atr.AtrCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.atr.AtrContext;
import org.chomookun.fintics.core.ohlcv.indicator.bolangerband.BollingerBandCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.bolangerband.BollingerBandContext;
import org.chomookun.fintics.core.ohlcv.indicator.cci.CciCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.cci.CciContext;
import org.chomookun.fintics.core.ohlcv.indicator.chaikinoscillator.ChaikinOscillatorCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.chaikinoscillator.ChaikinOscillatorContext;
import org.chomookun.fintics.core.ohlcv.indicator.dmi.DmiCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.dmi.DmiContext;
import org.chomookun.fintics.core.ohlcv.indicator.ema.EmaCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.ema.EmaContext;
import org.chomookun.fintics.core.ohlcv.indicator.keltnerchannel.KeltnerChannelCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.keltnerchannel.KeltnerChannelContext;
import org.chomookun.fintics.core.ohlcv.indicator.macd.MacdCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.macd.MacdContext;
import org.chomookun.fintics.core.ohlcv.indicator.obv.ObvCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.obv.ObvContext;
import org.chomookun.fintics.core.ohlcv.indicator.pricechannel.PriceChannelCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.pricechannel.PriceChannelContext;
import org.chomookun.fintics.core.ohlcv.indicator.rsi.RsiCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.rsi.RsiContext;
import org.chomookun.fintics.core.ohlcv.indicator.sma.SmaCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.sma.SmaContext;
import org.chomookun.fintics.core.ohlcv.indicator.stochasticslow.StochasticSlowCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.stochasticslow.StochasticSlowContext;
import org.chomookun.fintics.core.ohlcv.indicator.williamsr.WilliamsRCalculator;
import org.chomookun.fintics.core.ohlcv.indicator.williamsr.WilliamsRContext;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class IndicatorCalculatorFactory {

    @Getter
    private final static Map<Class<?>, Class<?>> registry = new LinkedHashMap<>();

    static {
        registry.put(SmaContext.class, SmaCalculator.class);
        registry.put(EmaContext.class, EmaCalculator.class);
        registry.put(BollingerBandContext.class, BollingerBandCalculator.class);
        registry.put(MacdContext.class, MacdCalculator.class);
        registry.put(RsiContext.class, RsiCalculator.class);
        registry.put(DmiContext.class, DmiCalculator.class);
        registry.put(ObvContext.class, ObvCalculator.class);
        registry.put(ChaikinOscillatorContext.class, ChaikinOscillatorCalculator.class);
        registry.put(AtrContext.class, AtrCalculator.class);
        registry.put(CciContext.class, CciCalculator.class);
        registry.put(StochasticSlowContext.class, StochasticSlowCalculator.class);
        registry.put(WilliamsRContext.class, WilliamsRCalculator.class);
        registry.put(PriceChannelContext.class, PriceChannelCalculator.class);
        registry.put(KeltnerChannelContext.class, KeltnerChannelCalculator.class);
    }

    /**
     * Get indicator calculator
     * @param context context
     * @return indicator calculator
     * @param <C> indicator context
     * @param <R> indicator
     * @param <T> indicator calculator
     */
    public static <C extends IndicatorContext, R extends Indicator, T extends IndicatorCalculator<C,R>> T getIndicator(C context) {
        Class<?> calculatorType = registry.get(context.getClass());
        IndicatorCalculator<?,?> calculator;
        try {
            Constructor<?> constructor = calculatorType.getConstructor(context.getClass());
            calculator = (IndicatorCalculator<?,?>) constructor.newInstance(context);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        return (T) calculator;
    }

}
