package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.PageableUtils;
import org.chomookun.arch4j.web.common.doc.PageableAsQueryParam;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.core.trade.model.TradeSearch;
import org.chomookun.fintics.core.trade.TradeService;
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

import java.util.List;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
@Slf4j
public class TradeRestController {

    private final TradeService tradeService;

    @Operation(summary = "Returns list of trade")
    @Parameter(name = "pageable", hidden = true) @PageableAsQueryParam
    @GetMapping
    public ResponseEntity<List<TradeResponse>> getTrades(
            @RequestParam(value = "name", required = false) String name,
            @PageableDefault Pageable pageable
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

    @Operation(summary = "Returns the specified trade")
    @GetMapping("{tradeId}")
    public ResponseEntity<TradeResponse> getTrade(@PathVariable("tradeId") String tradeId) {
        TradeResponse tradeResponse = tradeService.getTrade(tradeId)
                .map(TradeResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(tradeResponse);
    }

    @Operation(summary = "Creates new trade")
    @PostMapping
    @PreAuthorize("hasAuthority('trade:edit')")
    @Transactional
    public ResponseEntity<TradeResponse> createTrade(@RequestBody TradeRequest tradeRequest) {
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

    @Operation(summary = "Modifies the specified trade")
    @PutMapping("{tradeId}")
    @PreAuthorize("hasAuthority('trade:edit')")
    @Transactional
    public ResponseEntity<TradeResponse> modifyTrade(@PathVariable("tradeId") String tradeId, @RequestBody TradeRequest tradeRequest) {
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

    @Operation(summary = "Deletes the specified trade")
    @DeleteMapping("{tradeId}")
    @PreAuthorize("hasAuthority('trade:edit')")
    @Transactional
    public ResponseEntity<Void> deleteTrade(@PathVariable("tradeId") String tradeId) {
        tradeService.deleteTrade(tradeId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Changes trade sort")
    @PatchMapping("{tradeId}/sort")
    public ResponseEntity<Void> changeTradeSort(@PathVariable("tradeId") String tradeId, @RequestParam("sort") Integer sort) {
        tradeService.changeTradeSort(tradeId, sort);
        return ResponseEntity.ok().build();
    }

}
