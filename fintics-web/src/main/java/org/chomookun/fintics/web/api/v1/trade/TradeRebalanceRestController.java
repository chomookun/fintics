package org.chomookun.fintics.web.api.v1.trade;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.balance.model.Balance;
import org.chomookun.fintics.core.balance.model.BalanceAsset;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientFactory;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.order.OrderService;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.trade.TradeService;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.web.api.v1.basket.dto.BasketAssetResponse;
import org.chomookun.fintics.web.api.v1.order.dto.OrderRequest;
import org.chomookun.fintics.web.api.v1.order.dto.OrderResponse;
import org.chomookun.fintics.web.api.v1.trade.dto.RebalanceRequest;
import org.chomookun.fintics.web.api.v1.trade.dto.RebalanceResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

@Tag(name = "trade")
@RestController
@RequestMapping("/api/v1/trades/{tradeId}/rebalance")
@PreAuthorize("hasAuthority('trade')")
@RequiredArgsConstructor
@Slf4j
public class TradeRebalanceRestController {

    private final TradeService tradeService;

    private final BasketService basketService;

    private final BrokerService brokerService;

    private final BrokerClientFactory brokerClientFactory;

    private final OrderService orderService;

    @Operation(summary = "Rebalance trade asset")
    @PostMapping
    @Transactional
    public ResponseEntity<RebalanceResponse> submitTradeOrder(@PathVariable("tradeId") String tradeId, @RequestBody RebalanceRequest rebalanceRequest) throws InterruptedException {

        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        Basket basket = basketService.getBasket(trade.getBasketId()).orElseThrow();
        Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
        Balance balance = brokerClient.getBalance();

        BasketAsset basketAsset = basket.getBasketAsset(rebalanceRequest.getAssetId()).orElseThrow();
        BalanceAsset balanceAsset = balance.getBalanceAsset(rebalanceRequest.getAssetId()).orElse(null);

        OrderBook orderBook = brokerClient.getOrderBook(basketAsset);

        BigDecimal investAmount = trade.getInvestAmount();
        BigDecimal holdingWeight = basketAsset.getHoldingWeight();
        BigDecimal price = orderBook.getPrice();

        BigDecimal targetAmount = investAmount.multiply(holdingWeight)
                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32);
        BigDecimal targetQuantity = targetAmount
                .divide(price, MathContext.DECIMAL32)
                .setScale(0, RoundingMode.HALF_UP);

        BigDecimal currentQuantity = Optional.ofNullable(balanceAsset)
                .map(BalanceAsset::getQuantity)
                .orElse(BigDecimal.ZERO);

        BigDecimal diffQuantity = targetQuantity.subtract(currentQuantity);

        Order.Type orderType = null;
        BigDecimal orderQuantity = null;
        // rebalance not needed
        if (diffQuantity.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalStateException("No rebalance needed for asset: " + rebalanceRequest.getAssetId());
        }
        // buy
        if (diffQuantity.compareTo(BigDecimal.ZERO) > 0) {
            orderType = Order.Type.BUY;
            orderQuantity = diffQuantity;
        }
        // sell
        if (diffQuantity.compareTo(BigDecimal.ZERO) < 0) {
            orderType = Order.Type.SELL;
            orderQuantity = diffQuantity.abs();
        }

        // order kind and price
        Order.Kind orderKind = Order.Kind.LIMIT;
        BigDecimal tickPrice = orderBook.getTickPrice();
        BigDecimal orderPrice = switch (Objects.requireNonNull(orderType)) {
            case BUY -> orderBook.getBidPrice().add(tickPrice);
            case SELL -> orderBook.getAskPrice().subtract(tickPrice);
        };

        // submit
        Order order = Order.builder()
                .orderAt(Instant.now())
                .type(orderType)
                .kind(orderKind)
                .tradeId(tradeId)
                .assetId(basketAsset.getAssetId())
                .price(orderPrice)
                .quantity(orderQuantity)
                .build();
        brokerClient.submitOrder(basketAsset, order);
        order.setResult(Order.Result.COMPLETED);
        Order savedOrder = orderService.saveOrder(order);

        // response
        RebalanceResponse rebalanceResponse = RebalanceResponse.builder()
                .tradeId(tradeId)
                .basketAsset(BasketAssetResponse.from(basketAsset))
                .order(OrderResponse.from(savedOrder))
                .build();
        return ResponseEntity.ok(rebalanceResponse);
    }

}
