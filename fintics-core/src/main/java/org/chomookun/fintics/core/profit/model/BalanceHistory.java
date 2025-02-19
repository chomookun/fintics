package org.chomookun.fintics.core.profit.model;

import lombok.*;
import org.chomookun.fintics.core.profit.entity.BalanceHistoryEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceHistory {

    private String brokerId;

    private LocalDate date;

    private BigDecimal totalAmount;

    private BigDecimal cashAmount;

    private BigDecimal purchaseAmount;

    private BigDecimal valuationAmount;

    public static BalanceHistory from(BalanceHistoryEntity balanceHistoryEntity) {
        return BalanceHistory.builder()
                .brokerId(balanceHistoryEntity.getBrokerId())
                .date(balanceHistoryEntity.getDate())
                .totalAmount(balanceHistoryEntity.getTotalAmount())
                .cashAmount(balanceHistoryEntity.getCashAmount())
                .purchaseAmount(balanceHistoryEntity.getPurchaseAmount())
                .valuationAmount(balanceHistoryEntity.getValuationAmount())
                .build();
    }

}
