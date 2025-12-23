package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.balance.BalanceService;
import org.chomookun.fintics.core.balance.RealizedProfitService;
import org.chomookun.fintics.core.balance.model.Balance;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.web.api.v1.trade.dto.BalanceHistoryResponse;
import org.chomookun.fintics.web.api.v1.trade.dto.BalanceResponse;
import org.chomookun.fintics.web.api.v1.trade.dto.DividendProfitResponse;
import org.chomookun.fintics.web.api.v1.trade.dto.RealizedProfitResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "balance")
@RestController
@RequestMapping("/api/v1/trades/{tradeId}/realized-profits")
@PreAuthorize("hasAuthority('balance')")
@RequiredArgsConstructor
public class TradeRealizedProfitRestController {

    private final TradeService tradeService;

    private final RealizedProfitService realizedProfitService;

    @Operation(description = "Returns realized profits")
    @GetMapping
    public ResponseEntity<List<RealizedProfitResponse>> getRealizedProfits(
            @PathVariable("tradeId") String tradeId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        List<RealizedProfitResponse> realizedProfitResponses = realizedProfitService.getRealizedProfits(trade.getBrokerId(), dateFrom, dateTo).stream()
                .map(RealizedProfitResponse::from)
                .toList();
        return ResponseEntity.ok(realizedProfitResponses);
    }

}
