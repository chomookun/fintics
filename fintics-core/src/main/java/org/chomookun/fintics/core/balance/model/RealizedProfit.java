package org.chomookun.fintics.core.balance.model;

import lombok.*;
import org.chomookun.fintics.core.balance.entity.RealizedProfitEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class RealizedProfit {

    private String brokerId;

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

    public static RealizedProfit from(RealizedProfitEntity entity) {
        return RealizedProfit.builder()
                .brokerId(entity.getBrokerId())
                .date(entity.getDate())
                .symbol(entity.getSymbol())
                .name(entity.getName())
                .quantity(entity.getQuantity())
                .purchasePrice(entity.getPurchasePrice())
                .purchaseAmount(entity.getPurchaseAmount())
                .disposePrice(entity.getDisposePrice())
                .disposeAmount(entity.getDisposeAmount())
                .feeAmount(entity.getFeeAmount())
                .profitAmount(entity.getProfitAmount())
                .profitPercentage(entity.getProfitPercentage())
                .build();
    }

}
