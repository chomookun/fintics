package org.chomookun.fintics.web.api.v1.trade.dto;

import lombok.*;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.web.api.v1.basket.dto.BasketAssetResponse;
import org.chomookun.fintics.web.api.v1.order.dto.OrderResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RebalanceResponse {

    private String tradeId;

    private BasketAssetResponse basketAsset;

    private OrderResponse order;

}
