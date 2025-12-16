package org.chomookun.fintics.core.broker.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.core.asset.model.Asset;

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

    private BigDecimal price;

    private BigDecimal quantity;

    private BigDecimal orderableQuantity;

    private BigDecimal purchasePrice;

    public BigDecimal getPurchaseAmount() {
        return purchasePrice.multiply(quantity);
    }

    public BigDecimal getValuationPrice() {
        return price;
    }

    public BigDecimal getValuationAmount() {
        return price.multiply(quantity);
    }

    public BigDecimal getProfitAmount() {
        return getValuationAmount().subtract(getPurchaseAmount());
    }

    public BigDecimal getProfitPercentage() {
        return getProfitAmount().divide(getPurchaseAmount(), MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.FLOOR);
    }

}
