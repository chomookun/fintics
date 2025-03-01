package org.chomookun.fintics.core.basket.rebalance;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class BasketRebalanceAsset {

    private String symbol;

    private String name;

    private BigDecimal holdingWeight;

    private String remark;

    /**
     * Creates new instance of basket rebalance asset
     * @param symbol symbol
     * @param name name
     * @param holdingWeight holding weight
     * @param remark remark
     * @return basket asset
     */
    public static BasketRebalanceAsset of(String symbol, String name, BigDecimal holdingWeight, String remark) {
        return BasketRebalanceAsset.builder()
                .symbol(symbol)
                .name(name)
                .holdingWeight(holdingWeight)
                .remark(remark)
                .build();
    }

}
