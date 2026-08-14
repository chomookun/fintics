package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.trade.RebalanceAssetService;
import org.chomookun.fintics.web.api.v1.order.dto.OrderResponse;
import org.chomookun.fintics.web.api.v1.trade.dto.RebalanceRequest;
import org.chomookun.fintics.web.api.v1.trade.dto.RebalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades/{tradeId}/rebalance-asset")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
@Slf4j
public class RebalanceAssetRestController {

    private final RebalanceAssetService rebalanceAssetService;

    @Operation(summary = "Rebalance trade asset")
    @PostMapping
    @Transactional
    public ResponseEntity<RebalanceResponse> rebalanceAsset(@PathVariable("tradeId") String tradeId, @RequestBody RebalanceRequest rebalanceRequest) throws InterruptedException {
        Order rebalanceOrder = rebalanceAssetService.rebalanceAsset(tradeId, rebalanceRequest.getAssetId());
        RebalanceResponse rebalanceResponse = RebalanceResponse.builder()
                .order(OrderResponse.from(rebalanceOrder))
                .build();
        return ResponseEntity.ok(rebalanceResponse);
    }

}
