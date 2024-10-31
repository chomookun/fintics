package org.oopscraft.fintics.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DividendHistory {

    private LocalDate date;

    private String symbol;

    private String name;

    private BigDecimal holdingQuantity;

    private BigDecimal dividendAmount;

    private LocalDate paymentDate;

}
