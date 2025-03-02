package org.chomookun.fintics.core.strategy.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.CoreConfiguration;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.strategy.entity.StrategyEntity;
import org.chomookun.fintics.core.strategy.model.StrategySearch;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class StrategyRepositoryTest extends CoreTestSupport {

    final StrategyRepository strategyRepository;

    @Test
    void findAll() {
        // given
        StrategyEntity strategyEntity = StrategyEntity.builder()
                .strategyId("test")
                .name("test")
                .build();
        entityManager.persist(strategyEntity);
        entityManager.flush();
        entityManager.clear();
        // when
        StrategySearch strategySearch = StrategySearch.builder()
                .name(strategyEntity.getName())
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<StrategyEntity> strategyPage = strategyRepository.findAll(strategySearch, pageable);
        // then
        strategyPage.getContent().forEach(it -> {
            assertTrue(it.getName().contains(strategyEntity.getName()));
        });
    }

    @Test
    void findAllWithUnPage() {
        // given
        StrategyEntity strategyEntity = StrategyEntity.builder()
                .strategyId("test")
                .name("test")
                .build();
        entityManager.persist(strategyEntity);
        entityManager.flush();
        entityManager.clear();
        // when
        StrategySearch strategySearch = StrategySearch.builder()
                .name(strategyEntity.getName())
                .build();
        Pageable pageable = Pageable.unpaged();
        Page<StrategyEntity> strategyPage = strategyRepository.findAll(strategySearch, pageable);
        // then
        strategyPage.getContent().forEach(it -> {
            assertTrue(it.getName().contains(strategyEntity.getName()));
        });
    }

}