package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.web.api.v1.basket.dto.BasketResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades/{tradeId}/basket")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
@Slf4j
public class TradeBasketRestController {

    private final TradeService tradeService;

    private final BasketService basketService;

    @Operation(summary = "Returns the specified trade basket")
    @GetMapping
    public ResponseEntity<BasketResponse> getTradeBasket(@PathVariable("tradeId") String tradeId) throws InterruptedException {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        Basket basket = basketService.getBasket(trade.getBasketId())
                .orElseThrow();
        // populates allocated amount
        basket.getBasketAssets().forEach(basketAsset -> {
            // calculates allocated amount
            BigDecimal allocatedAmount = trade.getInvestAmount()
                    .multiply(basketAsset.getHoldingWeight())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            basketAsset.setAllocatedAmount(allocatedAmount);
        });
        // responses
        BasketResponse basketResponse = BasketResponse.from(basket);
        return ResponseEntity.ok(basketResponse);
    }

}
