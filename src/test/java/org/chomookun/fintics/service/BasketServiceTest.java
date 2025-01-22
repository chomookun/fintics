package org.chomookun.fintics.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.FinticsConfiguration;
import org.chomookun.fintics.dao.BasketAssetEntity;
import org.chomookun.fintics.dao.BasketEntity;
import org.chomookun.fintics.model.Basket;
import org.chomookun.fintics.model.BasketSearch;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class BasketServiceTest extends CoreTestSupport {

    private final BasketService basketService;

    @Test
    void getBaskets() {
        // given
        BasketEntity basketEntity = BasketEntity.builder()
                .basketId("test")
                .name("test name")
                .basketAssets(List.of(BasketAssetEntity.builder()
                        .basketId("test")
                        .assetId("US.APPL")
                        .build()))
                .build();
        entityManager.persist(basketEntity);
        // when
        BasketSearch basketSearch = BasketSearch.builder()
                .name("test")
                .build();
        Page<Basket> basketPage = basketService.getBaskets(basketSearch, Pageable.unpaged());
        // then
        basketPage.getContent().forEach(it -> {
            log.info("basketAssets:{}", it.getBasketAssets());
        });
    }

    @Test
    void getBasket() {
        // given
        BasketEntity basketEntity = BasketEntity.builder()
                .basketId("test")
                .name("test name")
                .build();
        entityManager.persist(basketEntity);
        // when
        Basket basket = basketService.getBasket("test").orElseThrow();
        // then
        log.info("basket: {}", basket);
    }

}