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

    private BigDecimal totalAmount;

    private BigDecimal realizedProfitAmount;

    private BigDecimal dividendAmount;

    @Builder.Default
    private List<RealizedProfit> realizedProfits = new ArrayList<>();

    @Builder.Default
    private List<DividendProfit> dividendProfits = new ArrayList<>();

}
