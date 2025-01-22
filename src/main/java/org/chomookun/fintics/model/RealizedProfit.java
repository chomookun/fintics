package org.chomookun.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class RealizedProfit {

    private LocalDate date;

    private String symbol;

    private String name;

    private BigDecimal quantity;

    private BigDecimal purchasePrice;

    private BigDecimal purchaseAmount;

    private BigDecimal disposePrice;

    private BigDecimal disposeAmount;

    private BigDecimal feeAmount;

    private BigDecimal profitAmount;

    private BigDecimal profitPercentage;

}
