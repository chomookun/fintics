package org.chomookun.fintics.core.trade.model;

import lombok.*;
import org.chomookun.fintics.core.order.model.Order;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ScaledOrder {

    private String tradeId;

    private String assetId;

    private Order.Type orderType;

    private BigDecimal quantity;

    private Integer count;

    private Integer step;

}
