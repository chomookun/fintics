package org.chomookun.fintics.web.api.v1.profit.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.profit.model.RealizedProfit;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class RealizedProfitResponse {

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

    public static RealizedProfitResponse from(RealizedProfit realizedProfit) {
        return RealizedProfitResponse.builder()
                .date(realizedProfit.getDate())
                .symbol(realizedProfit.getSymbol())
                .name(realizedProfit.getName())
                .quantity(realizedProfit.getQuantity())
                .purchasePrice(realizedProfit.getPurchasePrice())
                .purchaseAmount(realizedProfit.getPurchaseAmount())
                .disposePrice(realizedProfit.getDisposePrice())
                .disposeAmount(realizedProfit.getDisposeAmount())
                .feeAmount(realizedProfit.getFeeAmount())
                .profitAmount(realizedProfit.getProfitAmount())
                .profitPercentage(realizedProfit.getProfitPercentage())
                .build();
    }

}
