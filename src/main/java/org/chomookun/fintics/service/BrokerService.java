package org.chomookun.fintics.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.dao.BrokerEntity;
import org.chomookun.fintics.dao.BrokerRepository;
import org.chomookun.fintics.dao.TradeRepository;
import org.chomookun.fintics.model.Broker;
import org.chomookun.fintics.model.BrokerSearch;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BrokerService {

    private final BrokerRepository brokerRepository;

    private final TradeRepository tradeRepository;

    public Page<Broker> getBrokers(BrokerSearch brokerSearch, Pageable pageable) {
        Page<BrokerEntity> brokerEntityPage = brokerRepository.findAll(brokerSearch, pageable);
        List<Broker> brokers = brokerEntityPage.getContent().stream()
                .map(Broker::from)
                .toList();
        long total = brokerEntityPage.getTotalElements();
        return new PageImpl<>(brokers, pageable, total);
    }

    public Optional<Broker> getBroker(String brokerId) {
        return brokerRepository.findById(brokerId)
                .map(Broker::from);
    }

    @Transactional
    public Broker saveBroker(Broker broker) {
        BrokerEntity brokerEntity;
        if (broker.getBrokerId() == null) {
            brokerEntity = BrokerEntity.builder()
                    .brokerId(IdGenerator.uuid())
                    .build();
        } else {
            brokerEntity = brokerRepository.findById(broker.getBrokerId())
                    .orElseThrow();
        }
        brokerEntity.setName(broker.getName());
        brokerEntity.setBrokerClientId(broker.getBrokerClientId());
        brokerEntity.setBrokerClientProperties(Optional.ofNullable(broker.getBrokerClientProperties())
                .map(PbePropertiesUtil::encodePropertiesString)
                .orElse(null));
        BrokerEntity savedBrokerEntity = brokerRepository.saveAndFlush(brokerEntity);
        return Broker.from(savedBrokerEntity);
    }

    @Transactional
    public void deleteBroker(String brokerId) {
        BrokerEntity brokerEntity = brokerRepository.findById(brokerId).orElseThrow();

        // checks referenced by trade
        if (!tradeRepository.findAllByBrokerId(brokerEntity.getBrokerId()).isEmpty()) {
            throw new DataIntegrityViolationException("Referenced by existing trade");
        }

        // deletes
        brokerRepository.delete(brokerEntity);
        brokerRepository.flush();
    }

}
