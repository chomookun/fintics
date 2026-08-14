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
import org.chomookun.fintics.core.trade.model.ScaledOrder;
import org.chomookun.fintics.core.trade.model.Trade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScaledOrderService {

    private final TradeService tradeService;

    private final BasketService basketService;

    private final BrokerService brokerService;

    private final BrokerClientFactory brokerClientFactory;

    private final OrderService orderService;

    @Transactional
    public List<Order> submitScaledOrder(ScaledOrder scaledOrder) throws InterruptedException {
        Trade trade = tradeService.getTrade(scaledOrder.getTradeId()).orElseThrow();
        Basket basket = basketService.getBasket(trade.getBasketId()).orElseThrow();
        Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
        Balance balance = brokerClient.getBalance();
        BasketAsset basketAsset = basket.getBasketAsset(scaledOrder.getAssetId()).orElseThrow();
        BalanceAsset balanceAsset = balance.getBalanceAsset(scaledOrder.getAssetId()).orElse(null);
        OrderBook orderBook = brokerClient.getOrderBook(basketAsset);

        BigDecimal bidPrice = orderBook.getBidPrice();
        BigDecimal askPrice = orderBook.getAskPrice();
        BigDecimal tickPrice = orderBook.getTickPrice();
        BigDecimal scaledQuantity = scaledOrder.getQuantity()
                .divide(BigDecimal.valueOf(scaledOrder.getCount()), MathContext.DECIMAL32)
                .setScale(0, RoundingMode.FLOOR);

        List<Order> orders = new ArrayList<>();
        for (int i = 0; i < scaledOrder.getCount(); i ++ ) {
            int offset = i * scaledOrder.getStep();
            BigDecimal offsetPrice = BigDecimal.valueOf(offset).multiply(tickPrice);
            BigDecimal scaledPrice = switch (Objects.requireNonNull(scaledOrder.getOrderType())) {
                case BUY ->
                        bidPrice.subtract(offsetPrice);
                case SELL ->
                        askPrice.add(offsetPrice);
                default ->
                        throw new RuntimeException();
            };
            Order order = Order.builder()
                    .orderAt(Instant.now())
                    .type(scaledOrder.getOrderType())
                    .kind(Order.Kind.LIMIT)
                    .tradeId(trade.getTradeId())
                    .assetId(basketAsset.getAssetId())
                    .price(scaledPrice)
                    .quantity(scaledQuantity)
                    .build();
            orders.add(order);
        }

        return orders;
    }


}
