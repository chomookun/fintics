package org.chomookun.fintics.api.v1.dto;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.fintics.model.BasketAsset;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketAssetResponse extends AssetResponse {

    private String basketId;

    private String assetId;

    private Integer sort;

    private boolean fixed;

    private boolean enabled;

    private BigDecimal holdingWeight;

    private String variables;

    public static BasketAssetResponse from(BasketAsset basketAsset) {
        return BasketAssetResponse.builder()
                .basketId(basketAsset.getBasketId())
                .assetId(basketAsset.getAssetId())
                .symbol(basketAsset.getSymbol())
                .name(basketAsset.getName())
                .market(basketAsset.getMarket())
                .exchange(basketAsset.getExchange())
                .type(basketAsset.getType())
                .exchange(basketAsset.getExchange())
                .updatedDate(basketAsset.getUpdatedDate())
                .marketCap(basketAsset.getMarketCap())
                .per(basketAsset.getPer())
                .eps(basketAsset.getEps())
                .roe(basketAsset.getRoe())
                .dividendYield(basketAsset.getDividendYield())
                .dividendFrequency(basketAsset.getDividendFrequency())
                .fixed(basketAsset.isFixed())
                .enabled(basketAsset.isEnabled())
                .holdingWeight(basketAsset.getHoldingWeight())
                .variables(basketAsset.getVariables())
                .sort(basketAsset.getSort())
                .build();
    }

}
