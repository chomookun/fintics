package org.chomookun.fintics.core.dividend;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.dividend.entity.DividendEntity;
import org.chomookun.fintics.core.dividend.model.Dividend;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class DividendServiceTest extends CoreTestSupport {

    final DividendService dividendService;

    @Test
    void getDividends() {
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
        Pageable pageable = PageRequest.of(0, 10);
        List<Dividend> dividends = dividendService.getDividends(dividendEntity.getAssetId(), dividendEntity.getDate(), dividendEntity.getDate(), pageable);
        // then
        log.info("dividends: {}", dividends);
    }

    @Test
    void getDividendsWithUnPaged() {
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
        Pageable pageable = Pageable.unpaged();
        List<Dividend> dividends = dividendService.getDividends(dividendEntity.getAssetId(), dividendEntity.getDate(), dividendEntity.getDate(), pageable);
        // then
        log.info("dividends: {}", dividends);
    }

}