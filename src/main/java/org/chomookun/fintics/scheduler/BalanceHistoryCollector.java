package org.chomookun.fintics.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.client.broker.BrokerClient;
import org.chomookun.fintics.client.broker.BrokerClientFactory;
import org.chomookun.fintics.dao.BalanceHistoryEntity;
import org.chomookun.fintics.dao.BalanceHistoryRepository;
import org.chomookun.fintics.dao.BrokerRepository;
import org.chomookun.fintics.model.Balance;
import org.chomookun.fintics.model.Broker;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceHistoryCollector extends AbstractScheduler {

    private final BrokerRepository brokerRepository;

    private final BrokerClientFactory brokerClientFactory;

    private final BalanceHistoryRepository balanceProfitRepository;

    private final PlatformTransactionManager transactionManager;

    /**
     * Collects balance history
     */
    @Scheduled(initialDelay = 10_000, fixedDelay = 1_000 * 60 * 10)
    public void collect() {
        log.info("BalanceHistoryCollector - Start collect balance history.");
        List<Broker> brokers = brokerRepository.findAll().stream()
                .map(Broker::from)
                .toList();
        for (Broker broker : brokers) {
            try {
                saveBalanceHistory(broker);
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
        }
        log.info("BalanceHistoryCollector - End collect balance history.");
    }

    /**
     * Saves balance history
     * @param broker broker
     */
    void saveBalanceHistory(Broker broker) throws Throwable {
        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
        Balance balance = brokerClient.getBalance();
        BalanceHistoryEntity balanceHistoryEntity = BalanceHistoryEntity.builder()
                .brokerId(broker.getBrokerId())
                .date(LocalDate.now())
                .totalAmount(balance.getTotalAmount())
                .cashAmount(balance.getCashAmount())
                .purchaseAmount(balance.getPurchaseAmount())
                .valuationAmount(balance.getValuationAmount())
                .build();
        this.saveEntities("BalanceHistoryEntities", List.of(balanceHistoryEntity), transactionManager, balanceProfitRepository);
    }

}
