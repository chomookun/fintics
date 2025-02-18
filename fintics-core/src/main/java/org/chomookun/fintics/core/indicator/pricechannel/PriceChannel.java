package org.chomookun.fintics.core.indicator.pricechannel;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.indicator.Indicator;

import java.math.BigDecimal;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Getter
public class PriceChannel extends Indicator {

    private final BigDecimal upper;

    private final BigDecimal lower;

    private final BigDecimal middle;

}
