package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.oopscraft.fintics.model.Order;
import org.oopscraft.fintics.model.OrderType;
import org.oopscraft.fintics.model.OrderResult;
import org.oopscraft.fintics.model.OrderKind;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Getter
public class OrderResponse {

    private String id;

    private LocalDateTime orderAt;

    private OrderType orderType;

    private String tradeId;

    @Setter
    private String tradeName;

    private String assetId;

    private String assetName;

    private OrderKind orderKind;

    private BigDecimal quantity;

    private BigDecimal price;

    private OrderResult orderResult;

    private String errorMessage;

    public static OrderResponse from(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .orderAt(order.getOrderAt())
                .orderType(order.getOrderType())
                .tradeId(order.getTradeId())
                .assetId(order.getAssetId())
                .assetName(order.getAssetName())
                .orderKind(order.getOrderKind())
                .quantity(order.getQuantity())
                .price(order.getPrice())
                .orderResult(order.getOrderResult())
                .errorMessage(order.getErrorMessage())
                .build();
    }

}
