package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.PageableUtils;
import org.chomookun.arch4j.web.common.doc.PageableAsQueryParam;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.web.api.v1.order.dto.OrderRequest;
import org.chomookun.fintics.web.api.v1.order.dto.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
@Slf4j
public class TradeOrderRestController {

    private final TradeService tradeService;

    @Operation(summary = "Returns trade orders")
    @PageableAsQueryParam
    @GetMapping("{tradeId}/orders")
    public ResponseEntity<List<OrderResponse>> getTradeOrders(
            @PathVariable("tradeId") String tradeId,
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "type", required = false) Order.Type type,
            @RequestParam(value = "result", required = false) Order.Result result,
            @PageableDefault Pageable pageable
    ) {
        Page<Order> orderPage = tradeService.getOrders(tradeId, assetId, type, result, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(OrderResponse::from)
                .toList();
        long count = orderPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("orders", pageable, count))
                .body(orderResponses);
    }

    @Operation(summary = "Submits trade order")
    @PostMapping("{tradeId}/orders")
    @Transactional
    public ResponseEntity<OrderResponse> submitTradeOrder(@PathVariable("tradeId") String tradeId, @RequestBody OrderRequest orderRequest) {
        Order order = Order.builder()
                .orderAt(Instant.now())
                .type(orderRequest.getType())
                .kind(orderRequest.getKind())
                .tradeId(tradeId)
                .assetId(orderRequest.getAssetId())
                .quantity(orderRequest.getQuantity())
                .build();
        Order savedOrder = tradeService.submitOrder(order);
        OrderResponse savedOrderResponse = OrderResponse.from(savedOrder);
        return ResponseEntity.ok(savedOrderResponse);
    }

}
