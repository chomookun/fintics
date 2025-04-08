package org.chomookun.fintics.core.broker.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DividendProfit {

    private String assetId;

    private String symbol;

    private String name;

    private LocalDate date;

    private LocalDate paymentDate;

    private BigDecimal holdingQuantity;

    private BigDecimal dividendAmount;

}
