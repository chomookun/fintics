package org.chomookun.fintics.daemon.balance;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.data.IdGenerator;
import org.chomookun.fintics.core.balance.entity.DividendProfitEntity;
import org.chomookun.fintics.core.balance.model.DividendProfit;
import org.chomookun.fintics.core.balance.repository.DividendProfitRepository;
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
public class DividendProfitCollector extends AbstractTask {

    private final BrokerRepository brokerRepository;

    private final BrokerClientFactory brokerClientFactory;

    private final DividendProfitRepository dividendProfitRepository;

    private final PlatformTransactionManager transactionManager;

    @Scheduled(initialDelay = 10_000, fixedDelay = 1_000 * 60 * 60)
    public void collect() {
        List<Broker> brokers = brokerRepository.findAll().stream()
                .map(Broker::from)
                .toList();
        for (Broker broker : brokers) {
            try {
                collectDividendProfits(broker);
            } catch (Throwable e) {
                log.warn(e.getMessage(), e);
            }
        }
    }

    void collectDividendProfits(Broker broker) throws Exception {
        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
        LocalDate dateFrom = dividendProfitRepository.findLastDateByBrokerId(broker.getBrokerId())
                .orElse(LocalDate.now().minusYears(1));
        LocalDate dateTo = LocalDate.now();
        List<DividendProfit> dividendProfits = brokerClient.getDividendProfits(dateFrom, dateTo);
        List<DividendProfitEntity> dividendProfitEntities = dividendProfits.stream()
                .map(it -> {
                    String logicalHash = generateLogicalHash(it);
                    return DividendProfitEntity.builder()
                            .brokerId(broker.getBrokerId())
                            .logicalHash(logicalHash)
                            .date(it.getDate())
                            .assetId(it.getAssetId())
                            .symbol(it.getSymbol())
                            .name(it.getName())
                            .paymentDate(it.getPaymentDate())
                            .holdingQuantity(it.getHoldingQuantity())
                            .dividendAmount(it.getDividendAmount())
                            .taxAmount(it.getTaxAmount())
                            .netAmount(it.getNetAmount())
                            .build();
                }).collect(Collectors.toList());
        this.saveEntities("dividendProfitEntities", dividendProfitEntities, transactionManager, dividendProfitRepository);
    }

    String generateLogicalHash(DividendProfit dividendProfit) {
        String logicalKey = String.join("|", List.of(
                dividendProfit.getDate().format(DateTimeFormatter.BASIC_ISO_DATE),
                dividendProfit.getSymbol(),
                dividendProfit.getDividendAmount().toPlainString()));
        return IdGenerator.md5(logicalKey);
    }

}
