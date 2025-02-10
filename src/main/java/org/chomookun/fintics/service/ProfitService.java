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
        try {

            // total amount
            Balance balance = brokerClient.getBalance();
            BigDecimal totalAmount = balance.getTotalAmount();

            // balance histories (+ 1 days)
            List<BalanceHistory> balanceHistories = balanceHistoryRepository.findAllByBrokerId(brokerId, dateFrom.minusDays(1), dateTo).stream()
                    .map(BalanceHistory::from)
                    .toList();
            BigDecimal balanceProfitAmount = BigDecimal.ZERO;
            if (!balanceHistories.isEmpty()) {
                BigDecimal startTotalAmount = balanceHistories.get(balanceHistories.size() - 1).getTotalAmount();
                BigDecimal endTotalAmount = balanceHistories.get(0).getTotalAmount();
                balanceProfitAmount = endTotalAmount.subtract(startTotalAmount);
            }

            // realized profit
            List<RealizedProfit> realizedProfits = brokerClient.getRealizedProfits(dateFrom, dateTo);
            BigDecimal realizedProfitAmount = realizedProfits.stream()
                    .map(RealizedProfit::getProfitAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // dividend profit
            List<DividendProfit> dividendProfits = brokerClient.getDividendProfits(dateFrom, dateTo);
            BigDecimal dividendProfitAmount = dividendProfits.stream()
                    .map(DividendProfit::getDividendAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            // returns
            return Profit.builder()
                    .brokerId(brokerId)
                    .totalAmount(totalAmount)
                    .balanceProfitAmount(balanceProfitAmount)
                    .realizedProfitAmount(realizedProfitAmount)
                    .dividendProfitAmount(dividendProfitAmount)
                    .balanceHistories(balanceHistories)
                    .realizedProfits(realizedProfits)
                    .dividendProfits(dividendProfits)
                    .build();

        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

}
