package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.web.common.doc.PageableAsQueryParam;
import org.chomookun.arch4j.web.common.data.PageableUtils;
import org.chomookun.fintics.web.api.v1.broker.dto.BalanceResponse;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.core.trade.model.TradeSearch;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.web.api.v1.order.dto.OrderRequest;
import org.chomookun.fintics.web.api.v1.order.dto.OrderResponse;
import org.chomookun.fintics.web.api.v1.trade.dto.TradeAssetResponse;
import org.chomookun.fintics.web.api.v1.trade.dto.TradeRequest;
import org.chomookun.fintics.web.api.v1.trade.dto.TradeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/trades")
@PreAuthorize("hasAuthority('api.trades')")
@RequiredArgsConstructor
@Tag(name = "trade", description = "trade operations")
@Slf4j
public class TradesRestController {

    private final TradeService tradeService;

    /**
     * gets trades
     * @return list of trade
     */
    @GetMapping
    @Operation(description = "gets trades")
    public ResponseEntity<List<TradeResponse>> getTrades(
            @RequestParam(value = "name", required = false)
            @Parameter(description = "trade name")
                String name,
            @PageableDefault
            @Parameter(hidden = true)
                Pageable pageable
    ) {
        TradeSearch tradeSearch = TradeSearch.builder()
                .name(name)
                .build();
        Page<Trade> tradePage = tradeService.getTrades(tradeSearch, pageable);
        List<TradeResponse> tradeResponses = tradePage.getContent().stream()
                .map(TradeResponse::from)
                .toList();
        long total = tradePage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("asset", pageable, total))
                .body(tradeResponses);
    }

    /**
     * gets trade
     * @param tradeId trade id
     * @return trade info
     */
    @GetMapping("{tradeId}")
    @Operation(description = "gets trade")
    public ResponseEntity<TradeResponse> getTrade(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId
    ) {
        TradeResponse tradeResponse = tradeService.getTrade(tradeId)
                .map(TradeResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(tradeResponse);
    }

    /**
     * creates trade
     * @param tradeRequest trade request
     * @return created trade info
     */
    @PostMapping
    @PreAuthorize("hasAuthority('api.trades.edit')")
    @Transactional
    @Operation(description = "creates trade")
    public ResponseEntity<TradeResponse> createTrade(
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "trade request")
            TradeRequest tradeRequest
    ) {
        Trade trade = Trade.builder()
                .tradeId(tradeRequest.getTradeId())
                .name(tradeRequest.getName())
                .sort(tradeRequest.getSort())
                .enabled(tradeRequest.isEnabled())
                .interval(tradeRequest.getInterval())
                .threshold(tradeRequest.getThreshold())
                .startTime(tradeRequest.getStartAt())
                .endTime(tradeRequest.getEndAt())
                .investAmount(tradeRequest.getInvestAmount())
                .orderKind(tradeRequest.getOrderKind())
                .cashAssetId(tradeRequest.getCashAssetId())
                .cashBufferWeight(tradeRequest.getCashBufferWeight())
                .brokerId(tradeRequest.getBrokerId())
                .basketId(tradeRequest.getBasketId())
                .strategyId(tradeRequest.getStrategyId())
                .strategyVariables(tradeRequest.getStrategyVariables())
                .alarmId(tradeRequest.getAlarmId())
                .alarmOnError(tradeRequest.isAlarmOnError())
                .alarmOnOrder(tradeRequest.isAlarmOnOrder())
                .build();
        Trade savedTrade = tradeService.saveTrade(trade);
        TradeResponse savedTradeResponse = TradeResponse.from(savedTrade);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTradeResponse);
    }

    /**
     * modifies trade
     * @param tradeId trade id
     * @param tradeRequest trade info
     * @return modified trade info
     */
    @PutMapping("{tradeId}")
    @PreAuthorize("hasAuthority('api.trades.edit')")
    @Transactional
    @Operation(description = "modifies trade")
    public ResponseEntity<TradeResponse> modifyTrade(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId,
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "trade request")
                    TradeRequest tradeRequest
    ) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        trade.setName(tradeRequest.getName());
        trade.setSort(tradeRequest.getSort());
        trade.setEnabled(tradeRequest.isEnabled());
        trade.setInterval(tradeRequest.getInterval());
        trade.setThreshold(tradeRequest.getThreshold());
        trade.setStartTime(tradeRequest.getStartAt());
        trade.setEndTime(tradeRequest.getEndAt());
        trade.setInvestAmount(tradeRequest.getInvestAmount());
        trade.setOrderKind(tradeRequest.getOrderKind());
        trade.setCashAssetId(tradeRequest.getCashAssetId());
        trade.setCashBufferWeight(tradeRequest.getCashBufferWeight());
        trade.setBrokerId(tradeRequest.getBrokerId());
        trade.setBasketId(tradeRequest.getBasketId());
        trade.setStrategyId(tradeRequest.getStrategyId());
        trade.setStrategyVariables(tradeRequest.getStrategyVariables());
        trade.setAlarmId(tradeRequest.getAlarmId());
        trade.setAlarmOnError(tradeRequest.isAlarmOnError());
        trade.setAlarmOnOrder(tradeRequest.isAlarmOnOrder());
        Trade savedTrade = tradeService.saveTrade(trade);
        TradeResponse savedTradeResponse = TradeResponse.from(savedTrade);
        return ResponseEntity.ok(savedTradeResponse);
    }

    /**
     * deletes trade
     * @param tradeId trade id
     * @return void
     */
    @DeleteMapping("{tradeId}")
    @PreAuthorize("hasAuthority('api.trades.edit')")
    @Transactional
    @Operation(description = "deletes trade")
    public ResponseEntity<Void> deleteTrade(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId
    ) {
        tradeService.deleteTrade(tradeId);
        return ResponseEntity.ok().build();
    }

    /**
     * Changes trade sort
     * @param tradeId trade id
     * @param sort sort
     */
    @PatchMapping("{tradeId}/sort")
    public ResponseEntity<Void> changeTradeSort(@PathVariable("tradeId") String tradeId, @RequestParam("sort") Integer sort) {
        tradeService.changeTradeSort(tradeId, sort);
        return ResponseEntity.ok().build();
    }

    /**
     * get trade profiles
     * @param tradeId trade id
     * @return profiles
     */
    @GetMapping("{tradeId}/assets")
    public ResponseEntity<List<TradeAssetResponse>> getAssets(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId
    ) {
        List<TradeAssetResponse> tradeAssetResponses = tradeService.getTradeAssets(tradeId).stream()
                .map(TradeAssetResponse::from)
                .toList();
        return ResponseEntity.ok(tradeAssetResponses);
    }

    /**
     * gets trade orders
     * @param tradeId trade id
     * @param assetId asset id
     * @param type type
     * @param result result
     * @param pageable pageable
     * @return list of orders
     */
    @GetMapping("{tradeId}/orders")
    @Operation(description = "gets trade orders")
    @PageableAsQueryParam
    public ResponseEntity<List<OrderResponse>> getTradeOrders(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId,
            @RequestParam(value = "assetId", required = false)
            @Parameter(description = "asset id")
                    String assetId,
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
        Page<Order> orderPage = tradeService.getOrders(tradeId, assetId, type, result, pageable);
        List<OrderResponse> orderResponses = orderPage.getContent().stream()
                .map(OrderResponse::from)
                .toList();
        long count = orderPage.getTotalElements();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_RANGE, PageableUtils.toContentRange("orders", pageable, count))
                .body(orderResponses);
    }

    /**
     * submit order
     * @param tradeId trade id
     * @param orderRequest order request
     * @return saved order response
     */
    @PostMapping("{tradeId}/orders")
    @Transactional
    public ResponseEntity<OrderResponse> submitOrder(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId,
            @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "order request")
            OrderRequest orderRequest
    ) {
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

    /**
     * gets trade balance
     * @param tradeId trade id
     * @return balance
     */
    @GetMapping("{tradeId}/balance")
    @Operation(description = "gets trade balance")
    public ResponseEntity<BalanceResponse> getTradeBalance(
            @PathVariable("tradeId")
            @Parameter(description = "trade id")
                    String tradeId
    ) throws InterruptedException {
        BalanceResponse balanceResponse = tradeService.getBalance(tradeId)
                .map(BalanceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(balanceResponse);
    }

}
