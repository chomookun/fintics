package org.chomookun.fintics.web.api.v1.balance;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.balance.BalanceService;
import org.chomookun.fintics.core.balance.model.Balance;
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

@Deprecated
@Tag(name = "balance")
@RestController
@RequestMapping("/api/v1/balances")
@PreAuthorize("hasAuthority('balance')")
@RequiredArgsConstructor
public class BalanceRestController {

//    private final BalanceService balanceService;
//
//    @Operation(description = "Returns balance")
//    @GetMapping("{brokerId}")
//    public ResponseEntity<BalanceResponse> getProfits(@PathVariable("brokerId") String brokerId) {
//        Balance balance = balanceService.getBalance(brokerId).orElseThrow();
//        BalanceResponse balanceResponse = BalanceResponse.from(balance);
//        return ResponseEntity.ok(balanceResponse);
//    }
//
//    @Operation(description = "Returns balance histories")
//    @GetMapping("{brokerId}/balance-histories")
//    public ResponseEntity<List<BalanceHistoryResponse>> getBalanceHistories(
//            @PathVariable("brokerId") String brokerId,
//            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
//            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
//    ) {
//        List<BalanceHistoryResponse> balanceHistoryResponses = balanceService.getBalanceHistories(brokerId, dateFrom, dateTo).stream()
//                .map(BalanceHistoryResponse::from)
//                .toList();
//        return ResponseEntity.ok(balanceHistoryResponses);
//    }
//
//    @Operation(description = "Returns realized profits")
//    @GetMapping("{brokerId}/realized-profits")
//    public ResponseEntity<List<RealizedProfitResponse>> getRealizedProfits(
//            @PathVariable("brokerId") String brokerId,
//            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
//            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
//    ) {
//        List<RealizedProfitResponse> realizedProfitResponses = balanceService.getRealizedProfits(brokerId, dateFrom, dateTo).stream()
//                .map(RealizedProfitResponse::from)
//                .toList();
//        return ResponseEntity.ok(realizedProfitResponses);
//    }
//
//    @Operation(description = "Returns dividend profits")
//    @GetMapping("{brokerId}/dividend-profits")
//    public ResponseEntity<List<DividendProfitResponse>> getDividendProfits(
//            @PathVariable("brokerId") String brokerId,
//            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
//            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
//    ) {
//        List<DividendProfitResponse> dividendProfitResponses = balanceService.getDividendProfits(brokerId, dateFrom, dateTo).stream()
//                .map(DividendProfitResponse::from)
//                .toList();
//        return ResponseEntity.ok(dividendProfitResponses);
//    }

}
