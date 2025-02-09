package org.chomookun.fintics.service;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.client.broker.BrokerClient;
import org.chomookun.fintics.client.broker.BrokerClientFactory;
import org.chomookun.fintics.dao.BalanceHistoryRepository;
import org.chomookun.fintics.model.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfitService {

    private final BrokerService brokerService;

    private final BrokerClientFactory brokerClientFactory;

    private final BalanceHistoryRepository balanceHistoryRepository;

    /**
     * returns profit
     * @param brokerId broker id
     * @param dateFrom date from
     * @param dateTo date to
     * @return profit
     */
    public Profit getProfit(String brokerId, LocalDate dateFrom, LocalDate dateTo) {
        dateFrom = Optional.ofNullable(dateFrom).orElse(LocalDate.now().minusMonths(1));
        dateTo = Optional.ofNullable(dateTo).orElse(LocalDate.now());
        Broker broker = brokerService.getBroker(brokerId).orElseThrow();
        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
        List<RealizedProfit> realizedProfits;
        List<DividendProfit> dividendHistories;
        try {
            realizedProfits = brokerClient.getRealizedProfits(dateFrom, dateTo);
            dividendHistories = brokerClient.getDividendProfits(dateFrom, dateTo);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // realized profit amount
        BigDecimal realizedProfitAmount = realizedProfits.stream()
                .map(RealizedProfit::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // dividend amount
        BigDecimal dividendAmount = dividendHistories.stream()
                .map(DividendProfit::getDividendAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // total amount
        BigDecimal profitAmount = realizedProfitAmount.add(dividendAmount);

        // balance histories (+ 7 days)
        List<BalanceHistory> balanceHistories = balanceHistoryRepository.findAllByBrokerId(brokerId, dateFrom.minusWeeks(1), dateTo).stream()
                .map(BalanceHistory::from)
                .toList();

        // returns
        return Profit.builder()
                .brokerId(brokerId)
                .profitAmount(profitAmount)
                .realizedProfitAmount(realizedProfitAmount)
                .realizedProfits(realizedProfits)
                .dividendProfitAmount(dividendAmount)
                .dividendProfits(dividendHistories)
                .balanceHistories(balanceHistories)
                .build();
    }

}
