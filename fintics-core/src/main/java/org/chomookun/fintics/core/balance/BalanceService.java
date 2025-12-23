package org.chomookun.fintics.core.balance;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.asset.repository.AssetRepository;
import org.chomookun.fintics.core.balance.model.Balance;
import org.chomookun.fintics.core.balance.model.BalanceHistory;
import org.chomookun.fintics.core.balance.repository.DividendProfitRepository;
import org.chomookun.fintics.core.balance.repository.RealizedProfitRepository;
import org.chomookun.fintics.core.broker.model.Broker;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientFactory;
import org.chomookun.fintics.core.balance.repository.BalanceHistoryRepository;
import org.chomookun.fintics.core.balance.model.DividendProfit;
import org.chomookun.fintics.core.balance.model.Profit;
import org.chomookun.fintics.core.balance.model.RealizedProfit;
import org.chomookun.fintics.core.trade.model.Trade;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BalanceService {

    private final BrokerService brokerService;

    private final BrokerClientFactory brokerClientFactory;

    private final AssetRepository assetRepository;

    /**
     * Returns balance
     * @param brokerId broker id
     * @return balance
     */
    public Optional<Balance> getBalance(String brokerId) {
        try {
            Broker broker = brokerService.getBroker(brokerId).orElseThrow();
            BrokerClient brokerClient = brokerClientFactory.getObject(broker);
            Balance balance = brokerClient.getBalance();
            balance.getBalanceAssets().forEach(balanceAsset -> {
                assetRepository.findById(balanceAsset.getAssetId()).ifPresent(assetEntity -> {
                    balanceAsset.setMarket(assetEntity.getMarket());
                    balanceAsset.setType(assetEntity.getType());
                    balanceAsset.setExchange(assetEntity.getExchange());
                });
            });
            return Optional.of(balance);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

//    /**
//     * Returns profit
//     * @param brokerId broker id
//     * @param dateFrom date from
//     * @param dateTo date to
//     * @return profit
//     */
//    public Profit getProfit(String brokerId, LocalDate dateFrom, LocalDate dateTo) {
//        dateFrom = Optional.ofNullable(dateFrom).orElse(LocalDate.now().minusMonths(1));
//        dateTo = Optional.ofNullable(dateTo).orElse(LocalDate.now());
//        Broker broker = brokerService.getBroker(brokerId).orElseThrow();
//        BrokerClient brokerClient = brokerClientFactory.getObject(broker);
//        try {
//            // total amount
//            Balance balance = brokerClient.getBalance();
//            BigDecimal totalAmount = balance.getTotalAmount();
//            // balance histories (+ 1 days)
//            List<BalanceHistory> balanceHistories = balanceHistoryRepository.findAllByBrokerId(brokerId, dateFrom.minusDays(1), dateTo).stream()
//                    .map(BalanceHistory::from)
//                    .toList();
//            BigDecimal balanceProfitAmount = BigDecimal.ZERO;
//            BigDecimal balanceProfitPercentage = BigDecimal.ZERO;
//            if (!balanceHistories.isEmpty()) {
//                BigDecimal startTotalAmount = balanceHistories.get(balanceHistories.size() - 1).getTotalAmount();
//                BigDecimal endTotalAmount = balanceHistories.get(0).getTotalAmount();
//                balanceProfitAmount = endTotalAmount.subtract(startTotalAmount);
//                balanceProfitPercentage = balanceProfitAmount.divide(startTotalAmount, MathContext.DECIMAL32)
//                        .multiply(BigDecimal.valueOf(100))
//                        .setScale(4, RoundingMode.FLOOR);
//            }
//            // realized profit
//            List<RealizedProfit> realizedProfits = brokerClient.getRealizedProfits(dateFrom, dateTo);
//            BigDecimal realizedProfitAmount = realizedProfits.stream()
//                    .map(RealizedProfit::getProfitAmount)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//            BigDecimal realizedProfitPercentage = realizedProfitAmount.divide(totalAmount, MathContext.DECIMAL32)
//                    .multiply(BigDecimal.valueOf(100))
//                    .setScale(4, RoundingMode.FLOOR);
//            // dividend profit
//            List<DividendProfit> dividendProfits = brokerClient.getDividendProfits(dateFrom, dateTo);
//            BigDecimal dividendProfitAmount = dividendProfits.stream()
//                    .map(DividendProfit::getDividendAmount)
//                    .reduce(BigDecimal.ZERO, BigDecimal::add);
//            BigDecimal dividendProfitPercentage = dividendProfitAmount.divide(totalAmount, MathContext.DECIMAL32)
//                    .multiply(BigDecimal.valueOf(100))
//                    .setScale(4, RoundingMode.FLOOR);
//            // returns
//            return Profit.builder()
//                    .brokerId(brokerId)
//                    .totalAmount(totalAmount)
//                    .balanceProfitAmount(balanceProfitAmount)
//                    .balanceProfitPercentage(balanceProfitPercentage)
//                    .realizedProfitAmount(realizedProfitAmount)
//                    .realizedProfitPercentage(realizedProfitPercentage)
//                    .dividendProfitAmount(dividendProfitAmount)
//                    .dividendProfitPercentage(dividendProfitPercentage)
//                    .balanceHistories(balanceHistories)
//                    .realizedProfits(realizedProfits)
//                    .dividendProfits(dividendProfits)
//                    .build();
//
//        } catch (Throwable t) {
//            throw new RuntimeException(t);
//        }
//    }

}
