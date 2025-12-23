package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.balance.BalanceService;
import org.chomookun.fintics.core.balance.DividendProfitService;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.web.api.v1.trade.dto.DividendProfitResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades/{tradeId}/dividend-profits")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
public class TradeDividendProfitRestController {

    private final DividendProfitService dividendProfitService;

    private final TradeService tradeService;

    @Operation(description = "Returns dividend profits")
    @GetMapping
    public ResponseEntity<List<DividendProfitResponse>> getDividendProfits(
            @PathVariable("tradeId") String tradeId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        List<DividendProfitResponse> dividendProfitResponses = dividendProfitService.getDividendProfits(trade.getBrokerId(), dateFrom, dateTo).stream()
                .map(DividendProfitResponse::from)
                .toList();
        return ResponseEntity.ok(dividendProfitResponses);
    }

}
