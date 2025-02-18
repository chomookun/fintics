package org.chomookun.fintics.core.indicator.ema;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.Indicator;

import java.math.BigDecimal;

@SuperBuilder
@Getter
@ToString
public class Ema extends Indicator {

    private final BigDecimal value;

}
