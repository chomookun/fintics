package org.chomookun.fintics.core.broker;

import lombok.RequiredArgsConstructor;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinitionRegistry;
import org.chomookun.fintics.core.broker.entity.BrokerEntity;
import org.chomookun.fintics.core.broker.repository.BrokerRepository;
import org.chomookun.fintics.core.trade.repository.TradeRepository;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.model.BrokerSearch;
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

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    private final TradeRepository tradeRepository;

    public Page<Broker> getBrokers(BrokerSearch brokerSearch, Pageable pageable) {
        Page<BrokerEntity> brokerEntityPage = brokerRepository.findAll(brokerSearch, pageable);
        List<Broker> brokers = brokerEntityPage.getContent().stream()
                .map(Broker::from)
                .toList();
        brokers.forEach(this::populateBroker);
        long total = brokerEntityPage.getTotalElements();
        return new PageImpl<>(brokers, pageable, total);
    }

    public Optional<Broker> getBroker(String brokerId) {
        return brokerRepository.findById(brokerId)
                .map(brokerEntity -> {
                    Broker broker = Broker.from(brokerEntity);
                    populateBroker(broker);
                    return broker;
                });
    }

    void populateBroker(Broker broker) {
        brokerClientDefinitionRegistry.getBrokerClientDefinition(broker.getBrokerClientId()).ifPresent(brokerClientDefinition -> {
            broker.setMarket(brokerClientDefinition.getMarket());
            broker.setTimezone(brokerClientDefinition.getTimezone());
            broker.setCurrency(brokerClientDefinition.getCurrency());
        });
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
