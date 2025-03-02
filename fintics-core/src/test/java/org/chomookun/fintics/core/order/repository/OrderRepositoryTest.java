package org.chomookun.fintics.core.order.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.order.entity.OrderEntity;
import org.chomookun.fintics.core.order.model.OrderSearch;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class OrderRepositoryTest extends CoreTestSupport {

    final OrderRepository orderRepository;

    @Test
    void findAll() {
        // given
        OrderSearch orderSearch = OrderSearch.builder()
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        // when
        Page<OrderEntity> orderEntityPage = orderRepository.findAll(orderSearch, pageable);
        // then
        log.info("orderEntityPage: {}", orderEntityPage);
    }

    @Test
    void findAllWithUnPaged() {
        // given
        OrderSearch orderSearch = OrderSearch.builder()
                .build();
        Pageable pageable = Pageable.unpaged();
        // when
        Page<OrderEntity> orderEntityPage = orderRepository.findAll(orderSearch, pageable);
        // then
        log.info("orderEntityPage: {}", orderEntityPage);
    }

}