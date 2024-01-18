package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import org.oopscraft.fintics.model.BalanceAsset;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceAssetResponse {

    private String accountNo;

    private String assetId;

    private String assetName;

    private BigDecimal quantity;

    private BigDecimal orderableQuantity;

    private BigDecimal purchaseAmount;

    private BigDecimal valuationAmount;

    private BigDecimal profitAmount;

    public static BalanceAssetResponse from(BalanceAsset balanceAsset) {
        return BalanceAssetResponse.builder()
                .accountNo(balanceAsset.getAccountNo())
                .assetId(balanceAsset.getAssetId())
                .assetName(balanceAsset.getAssetName())
                .quantity(balanceAsset.getQuantity())
                .orderableQuantity(balanceAsset.getOrderableQuantity())
                .purchaseAmount(balanceAsset.getPurchaseAmount())
                .valuationAmount(balanceAsset.getValuationAmount())
                .profitAmount(balanceAsset.getProfitAmount())
                .build();
    }
}
