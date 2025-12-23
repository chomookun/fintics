package org.chomookun.fintics.daemon.balance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.execution.model.Execution;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientFactory;
import org.chomookun.fintics.core.balance.entity.BalanceHistoryEntity;
import org.chomookun.fintics.core.balance.repository.BalanceHistoryRepository;
import org.chomookun.fintics.core.broker.repository.BrokerRepository;
import org.chomookun.fintics.core.balance.model.Balance;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.daemon.common.AbstractTask;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BalanceHistoryCollector extends AbstractTask {

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
        try {
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
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
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
        this.saveEntities("balanceHistoryEntities", List.of(balanceHistoryEntity), transactionManager, balanceProfitRepository);
    }

}
