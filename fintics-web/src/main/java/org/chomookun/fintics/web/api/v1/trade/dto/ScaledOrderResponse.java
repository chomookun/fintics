package org.chomookun.fintics.web.api.v1.trade.dto;

import lombok.*;
import org.chomookun.fintics.web.api.v1.basket.dto.BasketAssetResponse;
import org.chomookun.fintics.web.api.v1.order.dto.OrderResponse;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScaledOrderResponse {

    private List<OrderResponse> orders;

}
