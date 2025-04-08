package org.chomookun.fintics.core.ohlcv.indicator.dmi;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.ohlcv.indicator.Indicator;

import java.math.BigDecimal;

@SuperBuilder
@Getter
@ToString
public class Dmi extends Indicator {

    private BigDecimal pdi;

    private BigDecimal mdi;

    private BigDecimal adx;

}
