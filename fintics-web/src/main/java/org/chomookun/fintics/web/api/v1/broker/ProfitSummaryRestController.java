package org.chomookun.fintics.web.api.v1.broker;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.broker.ProfitSummaryService;
import org.chomookun.fintics.core.broker.model.ProfitSummary;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.web.api.v1.broker.dto.ProfitSummaryResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/brokers/{brokerId}/profit-summary")
@PreAuthorize("hasAuthority('broker')")
@RequiredArgsConstructor
public class ProfitSummaryRestController {

    private final ProfitSummaryService profitSummaryService;

    @Operation(description = "Returns balance history summary")
    @GetMapping
    public ResponseEntity<ProfitSummaryResponse> getTradeProfitSummary(
            @PathVariable("brokerId") String brokerId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        ProfitSummary profitSummary = profitSummaryService.getProfitSummary(brokerId, dateFrom, dateTo);
        ProfitSummaryResponse profitSummaryResponse = ProfitSummaryResponse.from(profitSummary);
        return ResponseEntity.ok(profitSummaryResponse);
    }

}
