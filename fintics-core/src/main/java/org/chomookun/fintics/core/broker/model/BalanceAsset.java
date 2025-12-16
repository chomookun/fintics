package org.chomookun.fintics.core.broker.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.asset.model.Asset;
import org.springframework.security.core.parameters.P;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Optional;

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

    public BigDecimal getPurchaseAmount() {
        if (quantity != null) {
            return quantity.multiply(purchasePrice)
                    .setScale(getCurrency().getDefaultFractionDigits(), RoundingMode.HALF_UP);
        } else {
            return null;
        }
    }

    public BigDecimal getValuationPrice() {
        return getPrice();
    }

    public BigDecimal getValuationAmount() {
        if (quantity != null) {
            return quantity.multiply(getValuationPrice())
                    .setScale(getCurrency().getDefaultFractionDigits(), RoundingMode.HALF_UP);
        } else {
            return null;
        }
    }

    public BigDecimal getProfitAmount() {
        if (quantity != null) {
            return getValuationAmount().subtract(getPurchaseAmount());
        } else {
            return null;
        }
    }

    public BigDecimal getProfitPercentage() {
        if (quantity != null) {
            return getProfitAmount().divide(getPurchaseAmount(), MathContext.DECIMAL32)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.FLOOR);
        } else {
            return null;
        }
    }

}
