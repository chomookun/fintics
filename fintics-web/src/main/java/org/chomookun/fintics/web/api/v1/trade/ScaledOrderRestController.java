package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.trade.ScaledOrderService;
import org.chomookun.fintics.core.trade.model.ScaledOrder;
import org.chomookun.fintics.web.api.v1.order.dto.OrderResponse;
import org.chomookun.fintics.web.api.v1.trade.dto.ScaledOrderRequest;
import org.chomookun.fintics.web.api.v1.trade.dto.ScaledOrderResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades/{tradeId}/scaled-order")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
@Slf4j
public class ScaledOrderRestController {

    private final ScaledOrderService scaledOrderService;

    @Operation(summary = "Submits scaled order")
    @PostMapping
    @Transactional
    public ResponseEntity<ScaledOrderResponse> submitScaledOrder(@PathVariable("tradeId") String tradeId, @RequestBody ScaledOrderRequest scaledOrderRequest) throws InterruptedException {
        ScaledOrder scaledOrder = ScaledOrder.builder()
                .tradeId(tradeId)
                .assetId(scaledOrderRequest.getAssetId())
                .orderType(scaledOrderRequest.getOrderType())
                .quantity(scaledOrderRequest.getQuantity())
                .count(scaledOrderRequest.getCount())
                .step(scaledOrderRequest.getStep())
                .build();
        List<Order> orders = scaledOrderService.submitScaledOrder(scaledOrder);
        List<OrderResponse> orderResponses = orders.stream()
                .map(OrderResponse::from)
                .toList();
        ScaledOrderResponse scaledOrderResponse = ScaledOrderResponse.builder()
                .orders(orderResponses)
                .build();
        return ResponseEntity.ok(scaledOrderResponse);
    }

}
