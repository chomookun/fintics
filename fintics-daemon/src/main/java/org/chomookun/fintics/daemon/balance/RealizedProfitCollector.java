package org.chomookun.fintics.daemon.balance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.fintics.core.balance.entity.RealizedProfitEntity;
import org.chomookun.fintics.core.balance.model.RealizedProfit;
import org.chomookun.fintics.core.balance.repository.RealizedProfitRepository;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientFactory;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.repository.BrokerRepository;
import org.chomookun.fintics.daemon.common.AbstractTask;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class RealizedProfitCollector extends AbstractTask {

    private final BrokerRepository brokerRepository;

    private final BrokerClientFactory brokerClientFactory;

    private final RealizedProfitRepository realizedProfitRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 10_000, fixedDelay = 1_000 * 60)
    public void collect() {
        List<Broker> brokers = brokerRepository.findAll().stream()
                .map(Broker::from)
                .toList();
        for (Broker broker : brokers) {
            try {
                collectRealizedProfits(broker);
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    void collectRealizedProfits(Broker broker) throws Exception {
        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
        LocalDate dateFrom = realizedProfitRepository.findLastDateByBrokerId(broker.getBrokerId())
                .orElse(LocalDate.now().minusYears(1));
        LocalDate dateTo = LocalDate.now();
        List<RealizedProfit> realizedProfits = brokerClient.getRealizedProfits(dateFrom, dateTo);
        List<RealizedProfitEntity> realizedProfitEntities = realizedProfits.stream()
                .map(it -> {
                    String logicalHash = generateLogicalHash(it);
                    return RealizedProfitEntity.builder()
                            .brokerId(broker.getBrokerId())
                            .logicalHash(logicalHash)
                            .date(it.getDate())
                            .symbol(it.getSymbol())
                            .name(it.getName())
                            .quantity(it.getQuantity())
                            .purchasePrice(it.getPurchasePrice())
                            .purchaseAmount(it.getPurchaseAmount())
                            .disposePrice(it.getDisposePrice())
                            .disposeAmount(it.getDisposeAmount())
                            .feeAmount(it.getFeeAmount())
                            .profitAmount(it.getProfitAmount())
                            .profitPercentage(it.getProfitPercentage())
                            .build();
                }).collect(Collectors.toList());
        this.saveEntities("realizedProfitEntities", realizedProfitEntities, transactionManager, realizedProfitRepository);
    }

    String generateLogicalHash(RealizedProfit realizedProfit) {
        String logicalKey = String.join("|", List.of(
                realizedProfit.getDate().format(DateTimeFormatter.BASIC_ISO_DATE),
                realizedProfit.getSymbol(),
                realizedProfit.getQuantity().toPlainString(),
                realizedProfit.getPurchaseAmount().toPlainString(),
                realizedProfit.getDisposeAmount().toPlainString(),
                realizedProfit.getProfitAmount().toPlainString()));
        return IdGenerator.md5(logicalKey);
    }

}
