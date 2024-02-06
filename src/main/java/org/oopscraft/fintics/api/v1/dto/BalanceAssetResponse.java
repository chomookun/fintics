package org.oopscraft.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.oopscraft.fintics.model.Asset;
import org.oopscraft.fintics.model.BalanceAsset;

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

    private BigDecimal purchaseAmount;

    private BigDecimal valuationAmount;

    private BigDecimal profitAmount;

    public static BalanceAssetResponse from(BalanceAsset balanceAsset) {
        return BalanceAssetResponse.builder()
                .assetId(balanceAsset.getAssetId())
                .assetName(balanceAsset.getAssetName())
                .accountNo(balanceAsset.getAccountNo())
                .quantity(balanceAsset.getQuantity())
                .orderableQuantity(balanceAsset.getOrderableQuantity())
                .purchaseAmount(balanceAsset.getPurchaseAmount())
                .valuationAmount(balanceAsset.getValuationAmount())
                .profitAmount(balanceAsset.getProfitAmount())
                .type(balanceAsset.getType())
                .marketCap(balanceAsset.getMarketCap())
                .issuedShares(balanceAsset.getIssuedShares())
                .per(balanceAsset.getPer())
                .roe(balanceAsset.getRoe())
                .roa(balanceAsset.getRoa())
                .links(LinkResponse.from(balanceAsset.getLinks()))
                .build();
    }
}
