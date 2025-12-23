package org.chomookun.fintics.web.api.v1.trade.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.balance.model.BalanceAsset;
import org.chomookun.fintics.web.api.v1.asset.dto.AssetResponse;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceAssetResponse extends AssetResponse {

    private String accountNo;

    private BigDecimal quantity;

    private BigDecimal orderableQuantity;

    private BigDecimal purchasePrice;

    private BigDecimal purchaseAmount;

    private BigDecimal valuationPrice;

    private BigDecimal valuationAmount;

    private BigDecimal profitAmount;

    private BigDecimal profitPercentage;

    public static BalanceAssetResponse from(BalanceAsset balanceAsset) {
        return BalanceAssetResponse.builder()
                .assetId(balanceAsset.getAssetId())
                .symbol(balanceAsset.getSymbol())
                .name(balanceAsset.getName())
                .market(balanceAsset.getMarket())
                .exchange(balanceAsset.getExchange())
                .type(balanceAsset.getType())
                .accountNo(balanceAsset.getAccountNo())
                .quantity(balanceAsset.getQuantity())
                .orderableQuantity(balanceAsset.getOrderableQuantity())
                .purchasePrice(balanceAsset.getPurchasePrice())
                .purchaseAmount(balanceAsset.getPurchaseAmount())
                .valuationPrice(balanceAsset.getValuationPrice())
                .valuationAmount(balanceAsset.getValuationAmount())
                .profitAmount(balanceAsset.getProfitAmount())
                .profitPercentage(balanceAsset.getProfitPercentage())
                .type(balanceAsset.getType())
                .build();
    }
}
