package org.chomookun.fintics.core.basket.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.basket.entity.BasketEntity;
import org.chomookun.fintics.core.basket.model.BasketSearch;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class BasketRepositoryTest extends CoreTestSupport {

    final BasketRepository basketRepository;

    @Test
    void findAll() {
        // given
        BasketSearch basketSearch = BasketSearch.builder()
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        // when
        Page<BasketEntity> basketEntityPage = basketRepository.findAll(basketSearch, pageable);
        // then
        log.info("basketEntityPage: {}", basketEntityPage);
    }

    @Test
    void findAllWithUnPaged() {
        // given
        BasketSearch basketSearch = BasketSearch.builder()
                .build();
        Pageable pageable = Pageable.unpaged();
        // when
        Page<BasketEntity> basketEntityPage = basketRepository.findAll(basketSearch, pageable);
        // then
        log.info("basketEntityPage: {}", basketEntityPage);
    }

}