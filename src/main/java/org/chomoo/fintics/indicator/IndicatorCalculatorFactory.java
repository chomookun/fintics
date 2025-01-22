package org.chomoo.fintics.indicator;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.Map;

public class IndicatorCalculatorFactory {

    private final static Map<Class<?>, Class<?>> registry = new LinkedHashMap<>(){{
        put(SmaContext.class, SmaCalculator.class);
        put(EmaContext.class, EmaCalculator.class);
        put(BollingerBandContext.class, BollingerBandCalculator.class);
        put(MacdContext.class, MacdCalculator.class);
        put(RsiContext.class, RsiCalculator.class);
        put(DmiContext.class, DmiCalculator.class);
        put(ObvContext.class, ObvCalculator.class);
        put(ChaikinOscillatorContext.class, ChaikinOscillatorCalculator.class);
        put(AtrContext.class, AtrCalculator.class);
        put(CciContext.class, CciCalculator.class);
        put(StochasticSlowContext.class, StochasticSlowCalculator.class);
        put(WilliamsRContext.class, WilliamsRCalculator.class);
        put(PriceChannelContext.class, PriceChannelCalculator.class);
        put(KeltnerChannelContext.class, KeltnerChannelCalculator.class);
    }};

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
