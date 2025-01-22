package org.chomoo.fintics.service;

import lombok.RequiredArgsConstructor;
import org.chomoo.fintics.service.TradeService;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.chomoo.arch4j.core.common.data.IdGenerator;
import org.chomoo.arch4j.core.common.test.CoreTestSupport;
import org.chomoo.fintics.FinticsConfiguration;
import org.chomoo.fintics.dao.TradeEntity;
import org.chomoo.fintics.model.Trade;
import org.chomoo.fintics.model.TradeSearch;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = FinticsConfiguration.class)
@RequiredArgsConstructor
public class TradeServiceTest extends CoreTestSupport {

    private final TradeService tradeService;

    @Test
    @Order(1)
    void getTrades() {
        // given
        TradeEntity tradeEntity = TradeEntity.builder()
                .tradeId(IdGenerator.uuid())
                .name("test")
                .enabled(true)
                .build();
        entityManager.persist(tradeEntity);

        // when
        TradeSearch tradeSearch = TradeSearch.builder()
                .build();
        Pageable pageable = Pageable.unpaged();
        List<Trade> trades = tradeService.getTrades(tradeSearch, pageable).getContent();

        // then
        assertTrue(trades.size() > 0);
    }

    @Test
    @Order(1)
    void getTrade() {
        // given
        TradeEntity tradeEntity = TradeEntity.builder()
                .tradeId(IdGenerator.uuid())
                .name("test")
                .enabled(true)
                .build();
        entityManager.persist(tradeEntity);

        // when
        Trade trade = tradeService.getTrade(tradeEntity.getTradeId())
                .orElseThrow();

        // then
        assertEquals(tradeEntity.getTradeId(), trade.getTradeId());
    }

}
