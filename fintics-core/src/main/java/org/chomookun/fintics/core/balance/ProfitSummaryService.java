package org.chomookun.fintics.core.balance;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.balance.model.*;
import org.chomookun.fintics.core.balance.repository.BalanceHistoryRepository;
import org.chomookun.fintics.core.balance.repository.DividendProfitRepository;
import org.chomookun.fintics.core.balance.repository.RealizedProfitRepository;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.broker.model.Broker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProfitSummaryService {

    private final BrokerService brokerService;

    private final BalanceHistoryRepository balanceHistoryRepository;

    private final RealizedProfitRepository realizedProfitRepository;

    private final DividendProfitRepository dividendProfitRepository;

    public ProfitSummary getProfitSummary(String brokerId, LocalDate dateFrom, LocalDate dateTo) {
        Broker broker= brokerService.getBroker(brokerId).orElseThrow();
        List<BalanceHistory> balanceHistories = balanceHistoryRepository.findAllByBrokerId(brokerId, dateFrom, dateTo).stream()
                .map(BalanceHistory::from)
                .toList();
        BigDecimal startTotalAmount = BigDecimal.ZERO;
        BigDecimal endTotalAmount = BigDecimal.ZERO;
        BigDecimal balanceProfitAmount = BigDecimal.ZERO;
        BigDecimal balanceProfitPercentage = BigDecimal.ZERO;
        if (!balanceHistories.isEmpty()) {
            startTotalAmount = balanceHistories.get(balanceHistories.size() - 1).getTotalAmount();
            endTotalAmount = balanceHistories.get(0).getTotalAmount();
            balanceProfitAmount = endTotalAmount.subtract(startTotalAmount);
            balanceProfitPercentage = balanceProfitAmount.divide(startTotalAmount, MathContext.DECIMAL32)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(4, RoundingMode.FLOOR);
        }

        // realized profit
        List<RealizedProfit> realizedProfits = realizedProfitRepository.findAllByBrokerId(brokerId, dateFrom, dateTo).stream()
                .map(RealizedProfit::from)
                .toList();
        BigDecimal realizedProfitAmount = realizedProfits.stream()
                .map(RealizedProfit::getProfitAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal realizedProfitPercentage = realizedProfitAmount.divide(endTotalAmount, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.FLOOR);

        // dividend profit
        List<DividendProfit> dividendProfits = dividendProfitRepository.findAllByBrokerId(brokerId, dateFrom, dateTo).stream()
                .map(DividendProfit::from)
                .toList();
        BigDecimal dividendProfitAmount = dividendProfits.stream()
                .map(DividendProfit::getDividendAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dividendProfitPercentage = dividendProfitAmount.divide(endTotalAmount, MathContext.DECIMAL32)
                .multiply(BigDecimal.valueOf(100))
                .setScale(4, RoundingMode.FLOOR);
        BigDecimal dividendProfitTaxAmount = dividendProfits.stream()
                .map(DividendProfit::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal dividendProfitNetAmount = dividendProfits.stream()
                .map(DividendProfit::getNetAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // dividend taxable amount
        BigDecimal dividendProfitTaxableAmount = BigDecimal.ZERO;
        if (Objects.equals(broker.getMarket(), "KR")) {
            dividendProfitTaxableAmount = dividendProfitTaxAmount.divide(BigDecimal.valueOf(0.154), MathContext.DECIMAL32)
                    .setScale(0, RoundingMode.CEILING);
        } else {
            dividendProfitTaxableAmount = dividendProfitAmount;
        }

        // returns
        return ProfitSummary.builder()
                .brokerId(brokerId)
                .totalAmount(endTotalAmount)
                .balanceProfitAmount(balanceProfitAmount)
                .balanceProfitPercentage(balanceProfitPercentage)
                .realizedProfitAmount(realizedProfitAmount)
                .realizedProfitPercentage(realizedProfitPercentage)
                .dividendProfitAmount(dividendProfitAmount)
                .dividendProfitPercentage(dividendProfitPercentage)
                .dividendProfitTaxAmount(dividendProfitTaxAmount)
                .dividendProfitTaxableAmount(dividendProfitTaxableAmount)
                .dividendProfitNetAmount(dividendProfitNetAmount)
                .balanceHistories(balanceHistories)
                .realizedProfits(realizedProfits)
                .dividendProfits(dividendProfits)
                .build();
    }

}
