package org.oopscraft.fintics.trade.order;

import ch.qos.logback.classic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.oopscraft.arch4j.core.alarm.AlarmService;
import org.oopscraft.arch4j.core.data.IdGenerator;
import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.dao.OrderEntity;
import org.oopscraft.fintics.dao.OrderRepository;
import org.oopscraft.fintics.model.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

@Getter
public abstract class OrderOperator {

    private final TradeClient tradeClient;

    private final Trade trade;

    private final Balance balance;

    private final OrderBook orderBook;

    private final PlatformTransactionManager transactionManager;

    @Setter
    private OrderRepository orderRepository;

    @Setter
    private AlarmService alarmService;

    @Setter
    protected Logger log;

    protected OrderOperator(OrderOperatorContext context) {
        this.tradeClient = context.getTradeClient();
        this.trade = context.getTrade();
        this.balance = context.getBalance();
        this.orderBook = context.getOrderBook();
        this.transactionManager = context.getTransactionManager();
    }

    public abstract void buyTradeAsset(TradeAsset tradeAsset) throws InterruptedException;

    public abstract void sellTradeAsset(TradeAsset balanceAsset) throws InterruptedException;

    public BigDecimal calculateHoldRatioAmount(TradeAsset tradeAsset) {
        return balance.getTotalAmount()
                .divide(BigDecimal.valueOf(100), MathContext.DECIMAL32)
                .multiply(tradeAsset.getHoldRatio())
                .setScale(2, RoundingMode.HALF_UP);
    }

    protected void buyAssetByAmount(Asset asset, BigDecimal amount) throws InterruptedException {
        BigDecimal price = getOrderBook().getPrice();
        BigDecimal quantity = amount.divide(price, MathContext.DECIMAL32);
        buyAssetByQuantityAndPrice(asset, quantity, price);
    }

    protected void buyAssetByQuantityAndPrice(Asset asset, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        Order order = Order.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(LocalDateTime.now())
                .orderType(OrderType.BUY)
                .orderKind(getTrade().getOrderKind())
                .tradeId(trade.getTradeId())
                .assetId(asset.getAssetId())
                .assetName(asset.getAssetName())
                .quantity(quantity)
                .price(price)
                .build();

        // check waiting order exists
        Order waitingOrder = tradeClient.getWaitingOrders().stream()
                .filter(element ->
                        Objects.equals(element.getAssetId(), order.getAssetId())
                                && element.getOrderType() == order.getOrderType())
                .findFirst()
                .orElse(null);
        if(waitingOrder != null) {
            // if limit type order, amend order
            if(waitingOrder.getOrderKind() == OrderKind.LIMIT) {
                waitingOrder.setPrice(price);
                log.info("amend buy order:{}", waitingOrder);
                tradeClient.amendOrder(waitingOrder);
            }
            return;
        }

        // submit buy order
        try {
            log.info("submit buy order:{}", order);
            tradeClient.submitOrder(order);
            if (trade.isAlarmOnOrder()) {
                if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                    String subject = String.format("[%s]", trade.getTradeName());
                    String content = String.format("[%s] Buy %s", asset.getAssetName(), quantity);
                    alarmService.sendAlarm(trade.getAlarmId(), subject, content);
                }
            }
            order.setOrderResult(OrderResult.COMPLETED);
        } catch(Throwable e) {
            order.setOrderResult(OrderResult.FAILED);
            order.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            saveTradeOrder(order);
        }
    }

    protected void sellAssetByAmount(Asset asset, BigDecimal amount) throws InterruptedException {
        BigDecimal price = getOrderBook().getPrice();
        BigDecimal quantity = amount.divide(price, MathContext.DECIMAL32);
        sellAssetByQuantityAndPrice(asset, quantity, price);
    }

    protected void sellAssetByQuantityAndPrice(Asset asset, BigDecimal quantity, BigDecimal price) throws InterruptedException {
        Order order = Order.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(LocalDateTime.now())
                .orderType(OrderType.SELL)
                .orderKind(getTrade().getOrderKind())
                .tradeId(trade.getTradeId())
                .assetId(asset.getAssetId())
                .assetName(asset.getAssetName())
                .quantity(quantity)
                .price(price)
                .build();

        // check waiting order exists
        Order waitingOrder = tradeClient.getWaitingOrders().stream()
                .filter(element ->
                        Objects.equals(element.getAssetId(), order.getAssetId())
                                && element.getOrderType() == order.getOrderType())
                .findFirst()
                .orElse(null);
        if(waitingOrder != null) {
            // if limit type order, amend order
            if(waitingOrder.getOrderKind() == OrderKind.LIMIT) {
                waitingOrder.setPrice(price);
                log.info("amend sell order:{}", waitingOrder);
                tradeClient.amendOrder(waitingOrder);
            }
            return;
        }

        // submit sell order
        try {
            log.info("submit sell order:{}", order);
            tradeClient.submitOrder(order);
            if (trade.isAlarmOnOrder()) {
                if (trade.getAlarmId() != null && !trade.getAlarmId().isBlank()) {
                    String subject = String.format("[%s]", trade.getTradeName());
                    String content = String.format("[%s] Sell %s", asset.getAssetName(), quantity);
                    alarmService.sendAlarm(trade.getAlarmId(), subject, content);
                }
            }
            order.setOrderResult(OrderResult.COMPLETED);
        } catch(Throwable e) {
            order.setOrderResult(OrderResult.FAILED);
            order.setErrorMessage(e.getMessage());
            throw e;
        } finally {
            saveTradeOrder(order);
        }
    }

    private void saveTradeOrder(Order order) {
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager, transactionDefinition);
        transactionTemplate.executeWithoutResult(transactionStatus ->
                orderRepository.saveAndFlush(OrderEntity.builder()
                        .orderId(order.getOrderId())
                        .orderAt(order.getOrderAt())
                        .orderType(order.getOrderType())
                        .tradeId(order.getTradeId())
                        .assetId(order.getAssetId())
                        .assetName(order.getAssetName())
                        .orderKind(order.getOrderKind())
                        .quantity(order.getQuantity())
                        .price(order.getPrice())
                        .orderResult(order.getOrderResult())
                        .errorMessage(order.getErrorMessage())
                        .build()));
    }

}
