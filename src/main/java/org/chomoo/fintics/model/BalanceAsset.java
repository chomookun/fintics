package org.chomoo.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BalanceAsset extends Asset {

    private String accountNo;

    private BigDecimal quantity;

    private BigDecimal orderableQuantity;

    private BigDecimal purchasePrice;

    private BigDecimal purchaseAmount;

    private BigDecimal valuationPrice;

    private BigDecimal valuationAmount;

    private BigDecimal profitAmount;

    public BigDecimal getProfitPercentage() {
        if (profitAmount != null && purchaseAmount != null) {
            return getProfitAmount().divide(getPurchaseAmount(), MathContext.DECIMAL32)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        return null;
    }

}
