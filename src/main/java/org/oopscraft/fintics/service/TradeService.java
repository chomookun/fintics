package org.oopscraft.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.oopscraft.arch4j.core.common.data.IdGenerator;
import org.oopscraft.arch4j.core.common.pbe.PbePropertiesUtil;
import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientFactory;
import org.oopscraft.fintics.dao.*;
import org.oopscraft.fintics.model.*;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeRepository tradeRepository;

    private final TradeAssetRepository tradeAssetRepository;

    private final OrderRepository orderRepository;

    private final BasketService basketService;

    private final BrokerService brokerService;

    private final AssetService assetService;

    private final OrderService orderService;

    private final BrokerClientFactory brokerClientFactory;

    private final AssetRepository assetRepository;

    @PersistenceContext
    private final EntityManager entityManager;

    /**
     * gets trades
     * @param tradeSearch trade search
     * @param pageable pageable
     * @return page of trades
     */
    public Page<Trade> getTrades(TradeSearch tradeSearch, Pageable pageable) {
        Page<TradeEntity> tradeEntitiesPage = tradeRepository.findAll(tradeSearch, pageable);
        List<Trade> strategies = tradeEntitiesPage.getContent().stream()
                .map(Trade::from)
                .toList();
        long total = tradeEntitiesPage.getTotalElements();
        return new PageImpl<>(strategies, pageable, total);
    }

    /**
     * gets trade
     * @param tradeId trade id
     * @return trade
     */
    public Optional<Trade> getTrade(String tradeId) {
        return tradeRepository.findById(tradeId)
                .map(Trade::from);
    }

    /**
     * saves trade
     * @param trade trade
     * @return saved trade
     */
    @Transactional
    public Trade saveTrade(Trade trade) {
        final TradeEntity tradeEntity;
        if(trade.getTradeId() != null) {
            tradeEntity = tradeRepository.findById(trade.getTradeId()).orElseThrow();
        } else {
            tradeEntity = TradeEntity.builder()
                    .tradeId(IdGenerator.uuid())
                    .build();
        }
        tradeEntity.setName(trade.getName());
        tradeEntity.setEnabled(trade.isEnabled());
        tradeEntity.setInterval(trade.getInterval());
        tradeEntity.setThreshold(trade.getThreshold());
        tradeEntity.setStartAt(trade.getStartTime());
        tradeEntity.setEndAt(trade.getEndTime());
        tradeEntity.setInvestAmount(trade.getInvestAmount());
        tradeEntity.setOrderKind(trade.getOrderKind());
        tradeEntity.setCashAssetId(trade.getCashAssetId());
        tradeEntity.setCashBufferWeight(trade.getCashBufferWeight());
        tradeEntity.setBrokerId(trade.getBrokerId());
        tradeEntity.setBasketId(trade.getBasketId());
        tradeEntity.setStrategyId(trade.getStrategyId());
        tradeEntity.setStrategyVariables(Optional.ofNullable(trade.getStrategyVariables())
                .map(PbePropertiesUtil::encode)
                .orElse(null));
        tradeEntity.setAlarmId(trade.getAlarmId());
        tradeEntity.setAlarmOnError(trade.isAlarmOnError());
        tradeEntity.setAlarmOnOrder(trade.isAlarmOnOrder());

        // save and return
        TradeEntity savedTradeEntity = tradeRepository.saveAndFlush(tradeEntity);
        entityManager.refresh(savedTradeEntity);
        return Trade.from(savedTradeEntity);
    }

    /**
     * deletes trade
     * @param tradeId trade id
     */
    @Transactional
    public void deleteTrade(String tradeId) {
        tradeAssetRepository.deleteByTradeId(tradeId);
        tradeRepository.deleteById(tradeId);
        tradeRepository.flush();
    }

    /**
     * gets trade assets
     * @param tradeId trade id
     * @return trade assets
     */
    public List<TradeAsset> getTradeAssets(String tradeId) {
        Trade trade = getTrade(tradeId).orElseThrow();
        Basket basket = basketService.getBasket(trade.getBasketId()).orElseThrow();
        List<BasketAsset> basketAssets = basket.getBasketAssets();
        List<TradeAssetEntity> tradeAssetEntities = tradeAssetRepository.findAllByTradeId(tradeId);
        return basketAssets.stream()
                .map(basketAsset -> {
                    TradeAsset tradeAsset = TradeAsset.builder()
                            .tradeId(tradeId)
                            .assetId(basketAsset.getAssetId())
                            .name(basketAsset.getName())
                            .build();
                    TradeAssetEntity tradeAssetEntity = tradeAssetEntities.stream()
                            .filter(it -> Objects.equals(it.getAssetId(), basketAsset.getAssetId()))
                            .findFirst()
                            .orElse(null);
                    if (tradeAssetEntity != null) {
                       tradeAsset.setPreviousClose(tradeAssetEntity.getPreviousClose());
                       tradeAsset.setOpen(tradeAssetEntity.getOpen());
                       tradeAsset.setClose(tradeAssetEntity.getClose());
                       tradeAsset.setMessage(tradeAssetEntity.getMessage());
                    }
                    return tradeAsset;
                })
                .collect(Collectors.toList());
    }

    public Page<Order> getOrders(String tradeId, String assetId, Order.Type type, Order.Result result, Pageable pageable) {
        // order search
        OrderSearch orderSearch = OrderSearch.builder()
                .tradeId(tradeId)
                .assetId(assetId)
                .type(type)
                .result(result)
                .build();
        // sort
        Sort sort = Sort.by(OrderEntity_.ORDER_AT).descending();
        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);

        // find
        Page<OrderEntity> orderEntityPage = orderRepository.findAll(orderSearch, pageable);
        List<Order> orders = orderEntityPage.getContent().stream()
                .map(Order::from)
                .toList();
        long total = orderEntityPage.getTotalElements();
        return new PageImpl<>(orders, pageable, total);
    }

    public Optional<Balance> getBalance(String tradeId) throws InterruptedException {
        Trade trade = getTrade(tradeId).orElseThrow();
        if(trade.getBrokerId() != null) {
            Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
            BrokerClient brokerClient = brokerClientFactory.getObject(broker);
            Balance balance = brokerClient.getBalance();
            balance.getBalanceAssets().forEach(balanceAsset -> {
                assetRepository.findById(balanceAsset.getAssetId()).ifPresent(assetEntity -> {
                    balanceAsset.setMarket(assetEntity.getMarket());
                    balanceAsset.setType(assetEntity.getType());
                    balanceAsset.setExchange(assetEntity.getExchange());
                });
            });
            return Optional.of(balance);
        }else{
            return Optional.empty();
        }
    }

    /**
     * submit order
     * @param order order
     * @return submitted order
     */
    public Order submitOrder(Order order) {
        try {
            Trade trade = getTrade(order.getTradeId()).orElseThrow();
            Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
            BrokerClient brokerClient = brokerClientFactory.getObject(broker);
            Asset asset = assetService.getAsset(order.getAssetId()).orElseThrow();
            // price
            OrderBook orderBook = brokerClient.getOrderBook(asset);
            BigDecimal tickPrice = brokerClient.getTickPrice(asset, orderBook.getPrice());
            BigDecimal price = switch (order.getType()) {
                case BUY -> orderBook.getBidPrice().add(tickPrice);
                case SELL -> orderBook.getAskPrice().subtract(tickPrice);
            };
            order.setPrice(price);
            // submit
            brokerClient.submitOrder(asset, order);
            order.setResult(Order.Result.COMPLETED);
        } catch (Throwable e) {
            order.setResult(Order.Result.FAILED);
            throw new RuntimeException(e);
        } finally {
            orderService.saveOrder(order);
        }
        // return
        return order;
    }

}
