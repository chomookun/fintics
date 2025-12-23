package org.chomookun.fintics.core.balance.model;

import lombok.*;
import org.chomookun.fintics.core.balance.entity.BalanceHistoryEntity;

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

    /**
     * Converts balance history entity to balance history.
     * @param balanceHistoryEntity balance history entity
     * @return balance history
     */
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
