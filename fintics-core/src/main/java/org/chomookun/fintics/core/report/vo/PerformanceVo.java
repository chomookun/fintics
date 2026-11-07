package org.chomookun.fintics.core.report.vo;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PerformanceVo {

    private String brokerId;

    private String name;

    private BigDecimal startTotalAmount;

    private BigDecimal endTotalAmount;

    private BigDecimal realizedProfitAmount;

    private BigDecimal dividendProfitAmount;

    private BigDecimal valuationProfitAmount;

    private BigDecimal totalProfitAmount;

}
