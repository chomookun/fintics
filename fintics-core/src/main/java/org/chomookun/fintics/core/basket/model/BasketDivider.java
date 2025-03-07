package org.chomookun.fintics.core.basket.model;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.basket.entity.BasketDividerEntity;

@Builder
@Getter
public class BasketDivider {

    private String basketId;

    private String dividerId;

    private Integer sort;

    private String name;

    /**
     * Converts basket divider entity to basket divider
     * @param basketDividerEntity basket divider entity
     * @return basket divider
     */
    public static BasketDivider from(BasketDividerEntity basketDividerEntity) {
        return BasketDivider.builder()
                .basketId(basketDividerEntity.getBasketId())
                .dividerId(basketDividerEntity.getDividerId())
                .sort(basketDividerEntity.getSort())
                .name(basketDividerEntity.getName())
                .build();
    }

}
