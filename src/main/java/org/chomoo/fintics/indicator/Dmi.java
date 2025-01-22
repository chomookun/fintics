package org.chomoo.fintics.indicator;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@SuperBuilder
@Getter
@ToString
public class Dmi extends Indicator {

    private BigDecimal pdi;

    private BigDecimal mdi;

    private BigDecimal adx;

}
