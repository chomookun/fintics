package org.chomookun.fintics.core.indicator.keltnerchannel;

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
public class KeltnerChannel extends Indicator {

    private final BigDecimal center;

    private final BigDecimal upper;

    private final BigDecimal lower;

}
