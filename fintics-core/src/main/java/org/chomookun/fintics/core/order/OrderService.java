package org.chomookun.fintics.core.order;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.fintics.core.order.entity.OrderEntity;
import org.chomookun.fintics.core.order.repository.OrderRepository;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.order.model.OrderSearch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;

    private final ObjectMapper objectMapper;

    /**
     * gets order history
     * @param orderSearch order search condition
     * @param pageable pageable
     * @return page of order
     */
    public Page<Order> getOrders(OrderSearch orderSearch, Pageable pageable) {
        Page<OrderEntity> orderEntityPage = orderRepository.findAll(orderSearch, pageable);
        List<Order> orders = orderEntityPage.getContent().stream()
                .map(Order::from)
                .collect(Collectors.toList());
        long total = orderEntityPage.getTotalElements();
        return new PageImpl<>(orders, pageable, total);
    }

    /**
     * saves order history
     * @param order order
     * @return saved order
     */
    public Order saveOrder(Order order) {
        OrderEntity orderEntity;
        if(order.getOrderId() != null) {
            orderEntity = orderRepository.findById(order.getOrderId()).orElseThrow();
        }else {
            orderEntity = OrderEntity.builder()
                    .orderId(IdGenerator.uuid())
                    .build();
        }
        orderEntity.setOrderAt(order.getOrderAt());
        orderEntity.setTradeId(order.getTradeId());
        orderEntity.setAssetId(order.getAssetId());
        orderEntity.setAssetName(order.getAssetName());
        orderEntity.setType(order.getType());
        orderEntity.setKind(order.getKind());
        orderEntity.setPrice(order.getPrice());
        orderEntity.setQuantity(order.getQuantity());
        orderEntity.setPurchasePrice(order.getPurchasePrice());
        orderEntity.setRealizedProfitAmount(order.getRealizedProfitAmount());
        orderEntity.setBrokerOrderId(order.getBrokerOrderId());
        orderEntity.setResult(order.getResult());
        orderEntity.setErrorMessage(order.getErrorMessage());

        // strategy result
        try {
            String strategyResultData = objectMapper.writeValueAsString(order.getStrategyResult());
            orderEntity.setStrategyResultData(strategyResultData);
        } catch (JsonProcessingException ignore) {
            log.warn(ignore.getMessage());
        }

        OrderEntity savedOrderEntity = orderRepository.saveAndFlush(orderEntity);
        return Order.from(savedOrderEntity);
    }

}
