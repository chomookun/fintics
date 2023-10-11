package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.Balance;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceResponse {

    private String accountNo;

    private BigDecimal totalAmount;

    private BigDecimal cashAmount;

    private BigDecimal purchaseAmount;

    private BigDecimal valuationAmount;

    @Builder.Default
    private List<BalanceAssetResponse> balanceAssets = new ArrayList<>();

    public static BalanceResponse from(Balance balance) {
        BalanceResponse balanceResponse = BalanceResponse.builder()
                .accountNo(balance.getAccountNo())
                .totalAmount(balance.getTotalAmount())
                .cashAmount(balance.getCashAmount())
                .purchaseAmount(balance.getPurchaseAmount())
                .valuationAmount(balance.getValuationAmount())
                .build();
        List<BalanceAssetResponse> balanceAssetResponses = balance.getBalanceAssets().stream()
                .map(BalanceAssetResponse::from)
                .collect(Collectors.toList());
        balanceResponse.setBalanceAssets(balanceAssetResponses);
        return balanceResponse;
    }


}
