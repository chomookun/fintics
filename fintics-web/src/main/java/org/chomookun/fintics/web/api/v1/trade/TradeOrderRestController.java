package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.PageableUtils;
import org.chomookun.arch4j.web.common.doc.PageableAsQueryParam;
import org.chomookun.fintics.core.asset.AssetService;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientFactory;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.order.OrderService;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.order.model.OrderSearch;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.model.Trade;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades/{tradeId}/orders")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
@Slf4j
public class TradeOrderRestController {

    private final OrderService orderService;

    private final TradeService tradeService;

    private final BrokerService brokerService;

    private final AssetService assetService;

    private final BrokerClientFactory brokerClientFactory;

    @Operation(summary = "Returns trade orders")
    @PageableAsQueryParam
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getTradeOrders(
            @PathVariable("tradeId") String tradeId,
            @RequestParam(value = "orderAtFrom", required = false) Instant orderAtFrom,
            @RequestParam(value = "orderAtTo", required = false) Instant orderAtTo,
            @RequestParam(value = "assetId", required = false) String assetId,
            @RequestParam(value = "type", required = false) Order.Type type,
            @RequestParam(value = "result", required = false) Order.Result result,
            @PageableDefault Pageable pageable
    ) {
        // order search
        OrderSearch orderSearch = OrderSearch.builder()
                .tradeId(tradeId)
                .orderAtFrom(orderAtFrom)
                .orderAtTo(orderAtTo)
                .assetId(assetId)
                .type(type)
                .result(result)
                .build();
        Page<Order> orderPage = orderService.getOrders(orderSearch, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(OrderResponse::from)
                .toList();
        long count = orderPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("orders", pageable, count))
                .body(orderResponses);
    }

    @Operation(summary = "Submits trade order")
    @PostMapping
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

        try {
            Trade trade = tradeService.getTrade(order.getTradeId()).orElseThrow();
            Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
            BrokerClient brokerClient = brokerClientFactory.getObject(broker);
            Asset asset = assetService.getAsset(order.getAssetId()).orElseThrow();
            // price
            OrderBook orderBook = brokerClient.getOrderBook(asset);
            BigDecimal tickPrice = orderBook.getTickPrice();
            BigDecimal price = switch (order.getType()) {
                case BUY -> orderBook.getBidPrice().add(tickPrice);
                case SELL -> orderBook.getAskPrice().subtract(tickPrice);
            };
            order.setPrice(price);
            // submit
            brokerClient.submitOrder(asset, order);
            order.setResult(Order.Result.COMPLETED);
        } catch (Throwable e) {
            order.setResult(Order.Result.FAILED);
            throw new RuntimeException(e);
        } finally {
            orderService.saveOrder(order);
        }
        OrderResponse savedOrderResponse = OrderResponse.from(order);
        return ResponseEntity.ok(savedOrderResponse);
    }

}
