package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.web.api.v1.trade.dto.TradeAssetResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
@Slf4j
public class TradeAssetRestController {

    private final TradeService tradeService;

    @Operation(summary = "Returns list of trade asset")
    @GetMapping("{tradeId}/assets")
    public ResponseEntity<List<TradeAssetResponse>> getTradeAssets(@PathVariable("tradeId") String tradeId) {
        List<TradeAssetResponse> tradeAssetResponses = tradeService.getTradeAssets(tradeId).stream()
                .map(TradeAssetResponse::from)
                .toList();
        return ResponseEntity.ok(tradeAssetResponses);
    }

}
