package org.chomookun.fintics.web.api.v1.basket.dto;

import lombok.*;
import org.chomookun.fintics.core.basket.model.Basket;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketRequest {

    private String basketId;

    private String name;

    private String market;

    private boolean rebalanceEnabled;

    private String rebalanceSchedule;

    private Basket.Language language;

    private String variables;

    private String script;

    @Builder.Default
    private List<BasketAssetRequest> basketAssets = new ArrayList<>();

    @Builder.Default
    private List<BasketDividerRequest> basketDividers = new ArrayList<>();

}
