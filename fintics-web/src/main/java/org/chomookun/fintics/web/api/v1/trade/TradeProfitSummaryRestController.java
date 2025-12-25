package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.balance.ProfitSummaryService;
import org.chomookun.fintics.core.balance.model.ProfitSummary;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.web.api.v1.trade.dto.ProfitSummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades/{tradeId}/profit-summary")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
public class TradeProfitSummaryRestController {

    private final TradeService tradeService;

    private final ProfitSummaryService profitSummaryService;


    @Operation(description = "Returns balance history summary")
    @GetMapping
    public ResponseEntity<ProfitSummaryResponse> getTradeProfitSummary(
            @PathVariable("tradeId") String tradeId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        ProfitSummary profitSummary = profitSummaryService.getProfitSummary(trade.getBrokerId(), dateFrom, dateTo);
        ProfitSummaryResponse profitSummaryResponse = ProfitSummaryResponse.from(profitSummary);
        return ResponseEntity.ok(profitSummaryResponse);
    }

}
