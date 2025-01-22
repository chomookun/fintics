package org.chomookun.fintics.api.v1.dto;

import lombok.*;
import org.chomookun.fintics.model.Order;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderRequest {

    private Order.Type type;

    private String tradeId;

    private String assetId;

    private Order.Kind kind;

    private BigDecimal quantity;

}
