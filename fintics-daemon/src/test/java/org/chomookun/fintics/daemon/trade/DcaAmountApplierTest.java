package org.chomookun.fintics.daemon.trade;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.trade.entity.TradeEntity;
import org.chomookun.fintics.core.trade.model.Trade;
import org.chomookun.fintics.daemon.FinticsDaemonConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = FinticsDaemonConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class DcaAmountApplierTest extends CoreTestSupport {

    private final DcaAmountApplier dcaAmountApplier;

    @Test
    void applyDcaAmounts() {
        dcaAmountApplier.applyDcaAmounts();
    }

    @Test
    void applyDcaAmount() {
        // given
        TradeEntity tradeEntity = TradeEntity.builder()
                .tradeId(IdGenerator.uuid())
                .name("test-trade")
                .enabled(true)
                .investAmount(BigDecimal.valueOf(1000))
                .dcaEnabled(true)
                .dcaFrequency(Trade.DcaFrequency.DAILY)
                .dcaAmount(BigDecimal.valueOf(100))
                .build();
        entityManager.persist(tradeEntity);
        entityManager.flush();
        entityManager.clear();
        // when
        dcaAmountApplier.applyDcaAmount(tradeEntity);
        // then
        TradeEntity savedTradeEntity = entityManager.find(TradeEntity.class, tradeEntity.getTradeId());
        assertEquals(1100, savedTradeEntity.getInvestAmount().doubleValue(), "new invest amount is not matched.");
    }

}