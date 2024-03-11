package org.oopscraft.fintics.calculator;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@Getter
public class BollingerBandContext extends CalculateContext {

    public static final BollingerBandContext DEFAULT = BollingerBandContext.of(20, 2);

    private final int period;

    private final int sdMultiplier;

    public static BollingerBandContext of(int period, int sdMultiplier) {
        return BollingerBandContext.builder()
                .period(period)
                .sdMultiplier(sdMultiplier)
                .build();
    }

}
