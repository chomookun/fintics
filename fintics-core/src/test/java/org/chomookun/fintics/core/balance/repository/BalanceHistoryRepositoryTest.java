package org.chomookun.fintics.core.balance.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.balance.entity.BalanceHistoryEntity;
import org.chomookun.fintics.core.balance.repository.BalanceHistoryRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class BalanceHistoryRepositoryTest extends CoreTestSupport {

    final BalanceHistoryRepository balanceHistoryRepository;

    @Test
    void findAllByBrokerId() {
        // given
        String brokerId = "test";
        LocalDate date = LocalDate.now();
        BalanceHistoryEntity balanceHistoryEntity = BalanceHistoryEntity.builder()
                .brokerId(brokerId)
                .date(date)
                .cashAmount(BigDecimal.valueOf(1000))
                .purchaseAmount(BigDecimal.valueOf(1000))
                .valuationAmount(BigDecimal.valueOf(1100))
                .totalAmount(BigDecimal.valueOf(2100))
                .build();
        entityManager.persist(balanceHistoryEntity);
        entityManager.flush();
        entityManager.clear();
        // when
        List<BalanceHistoryEntity> balanceHistoryEntities = balanceHistoryRepository.findAllByBrokerId(brokerId, date, date);
        // then
        assertFalse(balanceHistoryEntities.isEmpty());
        balanceHistoryEntities.forEach(it -> {
            assertEquals(brokerId, it.getBrokerId());
            assertEquals(date, it.getDate());
        });
    }

}