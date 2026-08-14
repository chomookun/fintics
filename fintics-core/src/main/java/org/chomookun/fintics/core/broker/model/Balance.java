package org.chomookun.fintics.core.broker.model;

import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Balance {

    private String accountNo;

    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal cashAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal purchaseAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal valuationAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal profitAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal realizedProfitAmount = BigDecimal.ZERO;

    @Builder.Default
    private List<BalanceAsset> balanceAssets = new ArrayList<>();

    public Optional<BalanceAsset> getBalanceAsset(String assetId) {
        return balanceAssets.stream()
                .filter(balanceAsset ->
                    Objects.equals(balanceAsset.getAssetId(), assetId))
                .findFirst();
    }

}
