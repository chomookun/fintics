package org.oopscraft.fintics.indicator.chaikinoscillator;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.oopscraft.fintics.indicator.Indicator;

import java.math.BigDecimal;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class ChaikinOscillator extends Indicator {

    private final BigDecimal value;

    private final BigDecimal signal;

}
