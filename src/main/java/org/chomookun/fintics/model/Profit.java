package org.chomookun.fintics.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Profit {

    private String brokerId;

    private BigDecimal totalAmount;

    private BigDecimal balanceProfitAmount;

    private BigDecimal balanceProfitPercentage;

    private BigDecimal realizedProfitAmount;

    private BigDecimal realizedProfitPercentage;

    private BigDecimal dividendProfitAmount;

    private BigDecimal dividendProfitPercentage;

    @Builder.Default
    private List<BalanceHistory> balanceHistories = new ArrayList<>();

    @Builder.Default
    private List<RealizedProfit> realizedProfits = new ArrayList<>();

    @Builder.Default
    private List<DividendProfit> dividendProfits = new ArrayList<>();

}
