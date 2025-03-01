package org.chomookun.fintics.core.broker.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.test.CoreTestSupport;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.chomookun.fintics.core.broker.entity.BrokerEntity;
import org.chomookun.fintics.core.broker.model.BrokerSearch;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@SpringBootTest(classes = FinticsCoreConfiguration.class)
@RequiredArgsConstructor
@Slf4j
class BrokerRepositoryTest extends CoreTestSupport {

    final BrokerRepository brokerRepository;

    @Test
    void findAll() {
        // given
        BrokerSearch brokerSearch = BrokerSearch.builder()
                .build();
        Pageable pageable = PageRequest.of(0, 10);
        // when
        Page<BrokerEntity> brokerEntityPage = brokerRepository.findAll(brokerSearch, pageable);
        // then
        log.info("brokerEntityPage: {}", brokerEntityPage);
    }

    @Test
    void findAllWithUnPaged() {
        // given
        BrokerSearch brokerSearch = BrokerSearch.builder()
                .build();
        Pageable pageable = Pageable.unpaged();
        // when
        Page<BrokerEntity> brokerEntityPage = brokerRepository.findAll(brokerSearch, pageable);
        // then
        log.info("brokerEntityPage: {}", brokerEntityPage);
    }

}