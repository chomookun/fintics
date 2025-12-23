package org.chomookun.fintics.core.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.asset.repository.AssetRepository;
import org.chomookun.fintics.core.asset.AssetService;
import org.chomookun.fintics.core.balance.BalanceService;
import org.chomookun.fintics.core.balance.model.Balance;
import org.chomookun.fintics.core.balance.model.BalanceHistory;
import org.chomookun.fintics.core.balance.model.DividendProfit;
import org.chomookun.fintics.core.balance.model.RealizedProfit;
import org.chomookun.fintics.core.basket.model.Basket;
import org.chomookun.fintics.core.basket.model.BasketAsset;
import org.chomookun.fintics.core.basket.BasketService;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientFactory;
import org.chomookun.fintics.core.order.entity.OrderEntity;
import org.chomookun.fintics.core.order.entity.OrderEntity_;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.broker.model.OrderBook;
import org.chomookun.fintics.core.order.model.OrderSearch;
import org.chomookun.fintics.core.order.repository.OrderRepository;
import org.chomookun.fintics.core.order.OrderService;
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
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeService {

    private final TradeRepository tradeRepository;

    private final TradeAssetRepository tradeAssetRepository;

//    private final OrderRepository orderRepository;

    private final BasketService basketService;

//    private final BrokerService brokerService;
//
//    private final AssetService assetService;
//
//    private final OrderService orderService;
//
//    private final BrokerClientFactory brokerClientFactory;
//
//    private final AssetRepository assetRepository;

    @PersistenceContext
    private final EntityManager entityManager;
//    private final BalanceService balanceService;

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
        double finalSort = sort;
        // up
        if (sort < tradeEntity.getSort()) {
            finalSort = sort - 0.5;
        }
        // down
        if (sort > tradeEntity.getSort()) {
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

//    public Page<Order> getOrders(String tradeId, Instant orderAtFrom, Instant orderAtTo, String assetId, Order.Type type, Order.Result result, Pageable pageable) {
//        // order search
//        OrderSearch orderSearch = OrderSearch.builder()
//                .tradeId(tradeId)
//                .orderAtFrom(orderAtFrom)
//                .orderAtTo(orderAtTo)
//                .assetId(assetId)
//                .type(type)
//                .result(result)
//                .build();
//        // sort
//        Sort sort = Sort.by(OrderEntity_.ORDER_AT).descending();
//        pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sort);
//
//        // find
//        Page<OrderEntity> orderEntityPage = orderRepository.findAll(orderSearch, pageable);
//        List<Order> orders = orderEntityPage.getContent().stream()
//                .map(Order::from)
//                .toList();
//        long total = orderEntityPage.getTotalElements();
//        return new PageImpl<>(orders, pageable, total);
//    }
//
//    /**
//     * submit order
//     * @param order order
//     * @return submitted order
//     */
//    @Transactional
//    public Order submitOrder(Order order) {
//        try {
//            Trade trade = getTrade(order.getTradeId()).orElseThrow();
//            Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
//            BrokerClient brokerClient = brokerClientFactory.getObject(broker);
//            Asset asset = assetService.getAsset(order.getAssetId()).orElseThrow();
//            // price
//            OrderBook orderBook = brokerClient.getOrderBook(asset);
//            BigDecimal tickPrice = orderBook.getTickPrice();
//            BigDecimal price = switch (order.getType()) {
//                case BUY -> orderBook.getBidPrice().add(tickPrice);
//                case SELL -> orderBook.getAskPrice().subtract(tickPrice);
//            };
//            order.setPrice(price);
//            // submit
//            brokerClient.submitOrder(asset, order);
//            order.setResult(Order.Result.COMPLETED);
//        } catch (Throwable e) {
//            order.setResult(Order.Result.FAILED);
//            throw new RuntimeException(e);
//        } finally {
//            orderService.saveOrder(order);
//        }
//        // return
//        return order;
//    }
//
//    public Optional<Balance> getBalance(String tradeId) throws InterruptedException {
//        Trade trade = getTrade(tradeId).orElseThrow();
//        if(trade.getBrokerId() != null) {
//            Broker broker = brokerService.getBroker(trade.getBrokerId()).orElseThrow();
//            BrokerClient brokerClient = brokerClientFactory.getObject(broker);
//            Balance balance = brokerClient.getBalance();
//            balance.getBalanceAssets().forEach(balanceAsset -> {
//                assetRepository.findById(balanceAsset.getAssetId()).ifPresent(assetEntity -> {
//                    balanceAsset.setMarket(assetEntity.getMarket());
//                    balanceAsset.setType(assetEntity.getType());
//                    balanceAsset.setExchange(assetEntity.getExchange());
//                });
//            });
//            return Optional.of(balance);
//        }else{
//            return Optional.empty();
//        }
//    }
//
//    public List<BalanceHistory> getBalanceHistories(String tradeId, LocalDate dateFrom, LocalDate dateTo) {
//        Trade trade = getTrade(tradeId).orElseThrow();
//        return balanceService.getBalanceHistories(trade.getBrokerId(), dateFrom, dateTo);
//    }
//
//    public List<RealizedProfit> getRealizedProfits(String tradeId, LocalDate dateFrom, LocalDate dateTo) {
//        Trade trade = getTrade(tradeId).orElseThrow();
//        return balanceService.getRealizedProfits(trade.getBrokerId(), dateFrom, dateTo);
//    }
//
//    public List<DividendProfit> getDividendProfits(String tradeId, LocalDate dateFrom, LocalDate dateTo) {
//        Trade trade = getTrade(tradeId).orElseThrow();
//        return balanceService.getDividendProfits(trade.getBrokerId(), dateFrom, dateTo);
//    }

}
