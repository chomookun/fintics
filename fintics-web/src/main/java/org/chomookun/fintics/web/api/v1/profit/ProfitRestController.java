package org.chomookun.fintics.web.api.v1.profit;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.web.api.v1.profit.dto.ProfitResponse;
import org.chomookun.fintics.core.profit.model.Profit;
import org.chomookun.fintics.core.profit.ProfitService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "profit")
@RestController
@RequestMapping("/api/v1/profits")
@PreAuthorize("hasAuthority('profit')")
@RequiredArgsConstructor
public class ProfitRestController {

    private final ProfitService realizedProfitService;

    @Operation(description = "Returns the specified profit")
    @GetMapping("{brokerId}")
    public ResponseEntity<ProfitResponse> getProfits(
            @PathVariable("brokerId") String brokerId,
            @RequestParam(value = "dateFrom", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(value = "dateTo", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    ) {
        Profit realizedProfit = realizedProfitService.getProfit(brokerId, dateFrom, dateTo);
        ProfitResponse realizedProfitResponse = ProfitResponse.from(realizedProfit);
        return ResponseEntity.ok(realizedProfitResponse);
    }

}
