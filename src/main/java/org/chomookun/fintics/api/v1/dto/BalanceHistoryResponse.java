package org.chomookun.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.model.BalanceHistory;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class BalanceHistoryResponse {

    private String brokerId;

    private LocalDate date;

    private BigDecimal totalAmount;

    private BigDecimal cashAmount;

    private BigDecimal purchaseAmount;

    private BigDecimal valuationAmount;

    public static BalanceHistoryResponse from(BalanceHistory balanceHistory) {
        return BalanceHistoryResponse.builder()
                .brokerId(balanceHistory.getBrokerId())
                .date(balanceHistory.getDate())
                .totalAmount(balanceHistory.getTotalAmount())
                .cashAmount(balanceHistory.getCashAmount())
                .purchaseAmount(balanceHistory.getPurchaseAmount())
                .valuationAmount(balanceHistory.getValuationAmount())
                .build();
    }

}
