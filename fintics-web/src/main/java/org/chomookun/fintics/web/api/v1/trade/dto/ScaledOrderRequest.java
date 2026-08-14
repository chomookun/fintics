package org.chomookun.fintics.web.api.v1.trade.dto;

import lombok.*;
import org.chomookun.fintics.core.order.model.Order;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScaledOrderRequest {

    private String assetId;

    private Order.Type orderType;

    private BigDecimal quantity;

    private Integer count;

    private Integer step;

}
