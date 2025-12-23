package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.balance.BalanceService;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.web.api.v1.trade.dto.BalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades/{tradeId}/balance")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
@Slf4j
public class TradeBalanceRestController {

    private final TradeService tradeService;

    private final BalanceService balanceService;

    @Operation(summary = "Returns the specified trade balance")
    @GetMapping
    public ResponseEntity<BalanceResponse> getTradeBalance(@PathVariable("tradeId") String tradeId) throws InterruptedException {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        BalanceResponse balanceResponse = balanceService.getBalance(trade.getBrokerId())
                .map(BalanceResponse::from)
                .orElseThrow();
        return ResponseEntity.ok(balanceResponse);
    }

}
