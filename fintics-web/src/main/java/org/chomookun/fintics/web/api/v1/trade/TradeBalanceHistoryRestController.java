package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.balance.BalanceHistoryService;
import org.chomookun.fintics.core.balance.BalanceService;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.web.api.v1.trade.dto.BalanceHistoryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades/{tradeId}/balance-histories")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
public class TradeBalanceHistoryRestController {

    private final TradeService tradeService;

    private final BalanceHistoryService balanceHistoryService;

    @Operation(description = "Returns balance histories")
    @GetMapping
    public ResponseEntity<List<BalanceHistoryResponse>> getBalanceHistories(
            @PathVariable("tradeId") String tradeId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        List<BalanceHistoryResponse> balanceHistoryResponses = balanceHistoryService.getBalanceHistories(trade.getBrokerId(), dateFrom, dateTo).stream()
                .map(BalanceHistoryResponse::from)
                .toList();
        return ResponseEntity.ok(balanceHistoryResponses);
    }

}
