package org.chomookun.fintics.core.dividend.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.dividend.entity.DividendEntity;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class DividendRepositoryTest extends CoreTestSupport {

    final DividendRepository dividendRepository;

    @Test
    void findByAssetIdAndDateBetweenOrderByDateDesc() {
        // given
        DividendEntity dividendEntity = DividendEntity.builder()
                .assetId("US.MSFT")
                .date(LocalDate.now())
                .dividendPerShare(BigDecimal.valueOf(1.2))
                .build();
        entityManager.persist(dividendEntity);
        entityManager.flush();
        entityManager.clear();
        // when
        List<DividendEntity> dividendEntities = dividendRepository.findByAssetIdAndDateBetweenOrderByDateDesc(dividendEntity.getAssetId(), dividendEntity.getDate(), dividendEntity.getDate());
        // then
        assertFalse(dividendEntities.isEmpty());
    }

}