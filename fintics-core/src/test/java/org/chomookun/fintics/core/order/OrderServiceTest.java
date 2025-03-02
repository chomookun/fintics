package org.chomookun.fintics.core.order;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.order.entity.OrderEntity;
import org.chomookun.fintics.core.order.model.Order;
import org.chomookun.fintics.core.order.model.OrderSearch;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class OrderServiceTest extends CoreTestSupport {

    @PersistenceContext
    final EntityManager entityManager;

    final OrderService orderService;

    @Test
    void saveOrderForPersist() {
        // given
        Order order = Order.builder()
                .assetId("US.MSFT")
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .build();
        // when
        Order savedOrder = orderService.saveOrder(order);
        // then
        entityManager.clear();
        assertNotNull(entityManager.find(OrderEntity.class, savedOrder.getOrderId()));
    }

    @Test
    void saveOrderForMerge() {
        // given
        OrderEntity orderEntity = OrderEntity.builder()
                .orderId(IdGenerator.uuid())
                .orderAt(Instant.now())
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .build();
        entityManager.persist(orderEntity);
        entityManager.flush();
        entityManager.clear();
        // when
        Order order = Order.from(entityManager.find(OrderEntity.class, orderEntity.getOrderId()));
        order.setResult(Order.Result.COMPLETED);
        Order savedOrder = orderService.saveOrder(order);
        // then
        entityManager.clear();
        assertEquals(Order.Result.COMPLETED, entityManager.find(OrderEntity.class, savedOrder.getOrderId()).getResult());
    }

    @Test
    void getOrders() {
        // given
        String orderId = IdGenerator.uuid();
        OrderEntity orderEntity = OrderEntity.builder()
                .orderId(orderId)
                .orderAt(Instant.now())
                .type(Order.Type.BUY)
                .kind(Order.Kind.MARKET)
                .build();
        entityManager.persist(orderEntity);
        entityManager.flush();
        entityManager.clear();
        // when
        OrderSearch orderSearch = OrderSearch.builder()
                .orderAtFrom(orderEntity.getOrderAt())
                .orderAtTo(orderEntity.getOrderAt())
                .build();
        Pageable pageable = PageRequest.of(0,10);
        Page<Order> orderPage = orderService.getOrders(orderSearch, pageable);
        // then
        log.info("orderPage: {}", orderPage);
        assertTrue(orderPage.getContent().stream()
                .anyMatch(order ->
                        Objects.equals(order.getOrderId(), orderId)));
    }

}