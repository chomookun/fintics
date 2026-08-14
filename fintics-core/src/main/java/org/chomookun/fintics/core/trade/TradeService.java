package org.chomookun.fintics.core.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.core.asset.AssetService;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.broker.model.Balance;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientFactory;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.order.OrderService;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.order.model.OrderSearch;
import org.chomookun.fintics.core.trade.entity.TradeAssetEntity;
import org.chomookun.fintics.core.trade.entity.TradeEntity;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.core.trade.model.TradeAsset;
import org.chomookun.fintics.core.trade.model.TradeSearch;
import org.chomookun.fintics.core.trade.repository.TradeAssetRepository;
import org.chomookun.fintics.core.trade.repository.TradeRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeRepository tradeRepository;

    private final TradeAssetRepository tradeAssetRepository;

    private final AssetService assetService;

    private final BasketService basketService;

    private final BrokerService brokerService;

    private final BrokerClientFactory brokerClientFactory;

    private final OrderService orderService;

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
        tradeEntity.setSort(trade.getSort());
        tradeEntity.setEnabled(trade.isEnabled());
        tradeEntity.setInterval(trade.getInterval());
        tradeEntity.setThreshold(trade.getThreshold());
        tradeEntity.setStartAt(trade.getStartTime());
        tradeEntity.setEndAt(trade.getEndTime());
        tradeEntity.setInvestAmount(trade.getInvestAmount());
        tradeEntity.setDcaEnabled(trade.isDcaEnabled());
        tradeEntity.setDcaFrequency(trade.getDcaFrequency());
        tradeEntity.setDcaAmount(trade.getDcaAmount());
        tradeEntity.setOrderKind(trade.getOrderKind());
        tradeEntity.setCashAssetId(trade.getCashAssetId());
        tradeEntity.setCashBufferWeight(trade.getCashBufferWeight());
        tradeEntity.setBrokerId(trade.getBrokerId());
        tradeEntity.setBasketId(trade.getBasketId());
        tradeEntity.setStrategyId(trade.getStrategyId());
        tradeEntity.setStrategyVariables(Optional.ofNullable(trade.getStrategyVariables())
                .map(PbePropertiesUtil::encodePropertiesString)
                .orElse(null));
        tradeEntity.setNotifierId(trade.getNotifierId());
        tradeEntity.setNotifyOnError(trade.isNotifyOnError());
        tradeEntity.setNotifyOnOrder(trade.isNotifyOnOrder());
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
     * Changes trade sort
     * @param tradeId trade id
     * @param sort sort
     */
    @Transactional
    public void changeTradeSort(String tradeId, Integer sort) {
        TradeEntity tradeEntity = tradeRepository.findById(tradeId).orElseThrow();
        double originSort = Optional.ofNullable(tradeEntity.getSort()).orElse(0);
        double finalSort = sort;
        // up
        if (sort < originSort) {
            finalSort = sort - 0.5;
        }
        // down
        if (sort > originSort) {
            finalSort = sort + 0.5;
        }
        // updates sort
        List<TradeEntity> tradeEntities = tradeRepository.findAll();
        Map<String, Double> tradeSorts = new HashMap<>();
        for (TradeEntity tradeEntity1 : tradeEntities) {
            if (tradeEntity1.getTradeId().equals(tradeId)) {
                tradeSorts.put(tradeEntity1.getTradeId(), finalSort);
            } else {
                tradeSorts.put(tradeEntity1.getTradeId(), (double) tradeEntity1.getSort());
            }
        }
        List<String> sortedTradeIds = tradeSorts.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .toList();
        // updates
        for (int i = 0; i < sortedTradeIds.size(); i++) {
            tradeRepository.updateSort(sortedTradeIds.get(i), i);
        }
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
                    // populates trade asset data from entity
                    TradeAssetEntity tradeAssetEntity = tradeAssetEntities.stream()
                            .filter(it -> Objects.equals(it.getAssetId(), basketAsset.getAssetId()))
                            .findFirst()
                            .orElse(null);
                    if (tradeAssetEntity != null) {
                        tradeAsset.setDateTime(tradeAssetEntity.getDateTime());
                        tradeAsset.setPreviousClose(tradeAssetEntity.getPreviousClose());
                        tradeAsset.setOpen(tradeAssetEntity.getOpen());
                        tradeAsset.setClose(tradeAssetEntity.getClose());
                        tradeAsset.setVolume(tradeAssetEntity.getVolume());
                        tradeAsset.setMessage(tradeAssetEntity.getMessage());
                        tradeAsset.setStrategyResult(tradeAssetEntity.getStrategyResult());
                    }
                    return tradeAsset;
                })
                .collect(Collectors.toList());
    }

    /**
     * Returns trade basket
     * @param tradeId trade id
     * @return basket
     */
    public Optional<Basket> getBasket(String tradeId) {
        Trade trade = getTrade(tradeId).orElseThrow();
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
        return Optional.of(basket);
    }

    /**
     * Returns trade balance
     * @param tradeId trade id
     * @return balance
     */
    public Optional<Balance> getBalance(String tradeId) {
        Trade trade = getTrade(tradeId).orElseThrow();
        return brokerService.getBalance(trade.getBrokerId());
    }

    /**
     * Returns trade orders
     * @param tradeId trade id
     * @param orderSearch order search
     * @param pageable pageable
     * @return page of orders
     */
    public Page<Order> getOrders(String tradeId, OrderSearch orderSearch, Pageable pageable) {
        Trade trade = getTrade(tradeId).orElseThrow();
        orderSearch.setTradeId(trade.getTradeId());
        return orderService.getOrders(orderSearch, pageable);
    }

    /**
     * Submits trade order
     * @param tradeId trade id
     * @param order order
     * @return submitted order
     */
    public Order submitOrder(String tradeId, Order order) {
        try {
            order.setTradeId(tradeId);
            Trade trade = getTrade(order.getTradeId()).orElseThrow();
            Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
            BrokerClient brokerClient = brokerClientFactory.getObject(broker);
            Asset asset = assetService.getAsset(order.getAssetId()).orElseThrow();

            // price
            OrderBook orderBook = brokerClient.getOrderBook(asset);
            BigDecimal tickPrice = orderBook.getTickPrice();
            BigDecimal price = switch (order.getType()) {
                case BUY -> orderBook.getBidPrice().add(tickPrice);
                case SELL -> orderBook.getAskPrice().subtract(tickPrice);
            };
            order.setPrice(price);
            // cancels previous orders
            List<Order> previousOrders = brokerClient.getWaitingOrders();
            for (Order previousOrder : previousOrders) {
                previousOrder.setQuantity(BigDecimal.ZERO);
                brokerClient.amendOrder(asset, previousOrder);
            }
            // submit new order
            brokerClient.submitOrder(asset, order);
            order.setResult(Order.Result.COMPLETED);
        } catch (Throwable e) {
            order.setResult(Order.Result.FAILED);
            throw new RuntimeException(e);
        } finally {
            orderService.saveOrder(order);
        }
        return order;
    }

}
