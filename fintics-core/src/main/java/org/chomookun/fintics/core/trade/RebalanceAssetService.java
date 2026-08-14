package org.chomookun.fintics.core.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.broker.model.Balance;
import org.chomookun.fintics.core.broker.model.BalanceAsset;
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
import org.chomookun.fintics.core.trade.model.Trade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RebalanceAssetService {

    private final TradeService tradeService;

    private final BasketService basketService;

    private final BrokerService brokerService;

    private final OrderService orderService;

    private final BrokerClientFactory brokerClientFactory;

    /**
     * Rebalance asset
     * @param tradeId trade id
     * @param assetId asset id
     */
    public Order rebalanceAsset(String tradeId, String assetId) throws InterruptedException {
        Trade trade = tradeService.getTrade(tradeId).orElseThrow();
        Basket basket = basketService.getBasket(trade.getBasketId()).orElseThrow();
        Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
        Balance balance = brokerClient.getBalance();
        BasketAsset basketAsset = basket.getBasketAsset(assetId).orElseThrow();
        BalanceAsset balanceAsset = balance.getBalanceAsset(assetId).orElse(null);
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
            throw new IllegalStateException("No rebalance needed for asset: " + assetId);
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
        // defines order
        Order order = Order.builder()
                .orderAt(Instant.now())
                .type(orderType)
                .kind(orderKind)
                .tradeId(tradeId)
                .assetId(basketAsset.getAssetId())
                .price(orderPrice)
                .quantity(orderQuantity)
                .build();
        // cancels previous orders
        List<Order> previousOrders = brokerClient.getWaitingOrders();
        for (Order previousOrder : previousOrders) {
            previousOrder.setQuantity(BigDecimal.ZERO);
            brokerClient.amendOrder(basketAsset, previousOrder);
        }
        // submit new order
        brokerClient.submitOrder(basketAsset, order);
        order.setResult(Order.Result.COMPLETED);
        return orderService.saveOrder(order);
    }

}
