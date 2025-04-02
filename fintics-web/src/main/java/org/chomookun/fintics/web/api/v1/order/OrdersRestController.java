package org.chomookun.fintics.web.api.v1.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.PageableUtils;
import org.chomookun.fintics.web.api.v1.order.dto.OrderResponse;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.order.model.OrderSearch;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.core.order.OrderService;
import org.chomookun.fintics.core.trade.TradeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/orders")
@PreAuthorize("hasAuthority('api.orders')")
@RequiredArgsConstructor
@Tag(name = "order", description = "orders operations")
@Slf4j
public class OrdersRestController {

    private final OrderService orderService;

    private final TradeService tradeService;

    /**
     * gets list of order
     * @param tradeId trade id
     * @param assetId asset id
     * @param type type
     * @param result result
     * @param pageable pageable
     * @return list of order
     */
    @GetMapping
    @Operation(description = "gets list of order")
    public ResponseEntity<List<OrderResponse>> getOrders(
            @RequestParam(value = "orderAtFrom", required = false)
                    Instant orderAtFrom,
            @RequestParam(value = "orderAtTo", required = false)
                    Instant orderAtTo,
            @RequestParam(value = "tradeId", required = false)
            @Parameter(description = "trade id")
                    String tradeId,
            @RequestParam(value = "assetId", required = false)
            @Parameter(description = "asset id")
                    String assetId,
            @RequestParam(value = "assetName", required = false)
            @Parameter(description = "asset name")
                    String assetName,
            @RequestParam(value = "type", required = false)
            @Parameter(description = "type")
                    Order.Type type,
            @RequestParam(value = "result", required = false)
            @Parameter(description = "result")
                    Order.Result result,
            @PageableDefault
            @Parameter(hidden = true)
                    Pageable pageable
    ) {
        OrderSearch orderSearch = OrderSearch.builder()
                .orderAtFrom(orderAtFrom)
                .orderAtTo(orderAtTo)
                .tradeId(tradeId)
                .assetId(assetId)
                .assetName(assetName)
                .type(type)
                .result(result)
                .build();
        Page<Order> orderPage = orderService.getOrders(orderSearch, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(OrderResponse::from)
                .collect(Collectors.toList());
        // set trade name
        orderResponses.forEach(orderResponse ->
                orderResponse.setTradeName(tradeService.getTrade(orderResponse.getTradeId())
                        .map(Trade::getName)
                        .orElse("")));
        // response
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("order", pageable, orderPage.getTotalElements()))
                .body(orderResponses);
    }

    /**
     * Gets order
     * @param orderId order id
     * @return order
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable("orderId") String orderId) {
        Order order = orderService.getOrder(orderId);
        OrderResponse orderResponse = OrderResponse.from(order);
        // set trade name
        orderResponse.setTradeName(tradeService.getTrade(orderResponse.getTradeId())
                .map(Trade::getName)
                .orElse(""));
        // response
        return ResponseEntity.ok(orderResponse);
    }

}
