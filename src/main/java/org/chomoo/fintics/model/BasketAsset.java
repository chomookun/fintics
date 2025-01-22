package org.chomoo.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomoo.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomoo.fintics.dao.AssetEntity;
import org.chomoo.fintics.dao.BasketAssetEntity;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketAsset extends Asset {

    private String basketId;

    private String assetId;

    private Integer sort;

    private boolean fixed;

    private boolean enabled;

    private BigDecimal holdingWeight;

    private String variables;

    /**
     * gets specified value
     * @param name name
     * @return value
     */
    public String getVariable(String name) {
        return PbePropertiesUtil.loadProperties(variables)
                .getProperty(name, null);
    }

    /**
     * from factory method
     * @param basketAssetEntity basket asset entity
     * @return basket
     */
    public static BasketAsset from(BasketAssetEntity basketAssetEntity) {
        BasketAsset basketAsset = BasketAsset.builder()
                .basketId(basketAssetEntity.getBasketId())
                .assetId(basketAssetEntity.getAssetId())
                .sort(basketAssetEntity.getSort())
                .fixed(basketAssetEntity.isFixed())
                .enabled(basketAssetEntity.isEnabled())
                .holdingWeight(basketAssetEntity.getHoldingWeight())
                .variables(basketAssetEntity.getVariables())
                .build();

        // asset entity
        AssetEntity assetEntity = basketAssetEntity.getAssetEntity();
        if(assetEntity != null) {
            basketAsset.setName(assetEntity.getName());
            basketAsset.setMarket(assetEntity.getMarket());
            basketAsset.setExchange(assetEntity.getExchange());
            basketAsset.setType(assetEntity.getType());
            basketAsset.setUpdatedDate(assetEntity.getUpdatedDate());
            basketAsset.setMarketCap(assetEntity.getMarketCap());
            basketAsset.setEps(assetEntity.getEps());
            basketAsset.setPer(assetEntity.getPer());
            basketAsset.setRoe(assetEntity.getRoe());
            basketAsset.setRoa(assetEntity.getRoa());
            basketAsset.setDividendYield(assetEntity.getDividendYield());
            basketAsset.setDividendFrequency(assetEntity.getDividendFrequency());
        }

        // return
        return basketAsset;
    }

}
