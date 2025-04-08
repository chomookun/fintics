package org.chomookun.fintics.core.ohlcv.indicator.rsi;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.ohlcv.indicator.Indicator;

import java.math.BigDecimal;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class Rsi extends Indicator {

    private final BigDecimal value;

    private final BigDecimal signal;

}
