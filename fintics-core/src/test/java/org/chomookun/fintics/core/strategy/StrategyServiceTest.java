package org.chomookun.fintics.core.strategy;

import kotlin.reflect.KClasses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.strategy.entity.StrategyEntity;
import org.chomookun.fintics.core.strategy.model.Strategy;
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
class StrategyServiceTest extends CoreTestSupport {

    final StrategyService strategyService;

    @Test
    void saveStrategy() {
        // when
        Strategy strategy = Strategy.builder()
                .strategyId("test")
                .name("Test")
                .build();
        Strategy savedStrategy = strategyService.saveStrategy(strategy);
        // then
        StrategyEntity savedStrategyEntity = entityManager.find(StrategyEntity.class, savedStrategy.getStrategyId());
        assertNotNull(savedStrategyEntity);
        assertEquals(savedStrategy.getStrategyId(), savedStrategyEntity.getStrategyId());
        assertEquals(savedStrategy.getName(), savedStrategyEntity.getName());
    }

    @Test
    void saveStrategyForMerge() {
        // given
        StrategyEntity strategyEntity = StrategyEntity.builder()
                .strategyId("test")
                .name("Test")
                .build();
        entityManager.persist(strategyEntity);
        entityManager.flush();
        // when
        entityManager.refresh(strategyEntity);
        Strategy strategy = Strategy.from(strategyEntity);
        strategy.setName("Changed");
        Strategy savedStrategy = strategyService.saveStrategy(strategy);
        // then
        StrategyEntity savedStrategyEntity = entityManager.find(StrategyEntity.class, savedStrategy.getStrategyId());
        assertEquals("Changed", savedStrategyEntity.getName());
    }

    @Test
    void getStrategy() {
        // given
        StrategyEntity strategyEntity = StrategyEntity.builder()
                .strategyId("test")
                .name("Test")
                .build();
        entityManager.persist(strategyEntity);
        entityManager.flush();
        // when
        Strategy strategy = strategyService.getStrategy(strategyEntity.getStrategyId()).orElse(null);
        // then
        assertNotNull(strategy);
        assertEquals(strategyEntity.getStrategyId(), strategy.getStrategyId());
        assertEquals(strategyEntity.getName(), strategy.getName());
    }

    @Test
    void deleteStrategy() {
        // given
        StrategyEntity strategyEntity = StrategyEntity.builder()
                .strategyId("test")
                .name("Test")
                .build();
        entityManager.persist(strategyEntity);
        entityManager.flush();
        // when
        strategyService.deleteStrategy(strategyEntity.getStrategyId());
        // then
        assertNull(entityManager.find(StrategyEntity.class, strategyEntity.getStrategyId()));
    }

    @Test
    void getStrategies() {
        // given
        StrategyEntity strategyEntity = StrategyEntity.builder()
                .strategyId("test")
                .name("Test")
                .build();
        entityManager.persist(strategyEntity);
        entityManager.flush();
        // when
        StrategySearch strategySearch = StrategySearch.builder()
                .name(strategyEntity.getName())
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Strategy> strategyPage = strategyService.getStrategies(strategySearch, pageable);
        // then
        strategyPage.getContent().forEach(it -> {
            assertTrue(it.getName().contains(strategySearch.getName()));
        });
    }
}