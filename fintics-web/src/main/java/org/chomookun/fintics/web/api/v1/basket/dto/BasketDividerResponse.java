package org.chomookun.fintics.web.api.v1.basket.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.basket.model.BasketDivider;

@Builder
@Getter
public class BasketDividerResponse {

    private String basketId;

    private String dividerId;

    private Integer sort;

    private String name;

    public static BasketDividerResponse from(BasketDivider basketDivider) {
        return BasketDividerResponse.builder()
                .basketId(basketDivider.getBasketId())
                .dividerId(basketDivider.getDividerId())
                .sort(basketDivider.getSort())
                .name(basketDivider.getName())
                .build();
    }

}
