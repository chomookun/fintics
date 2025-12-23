package org.chomookun.fintics.core.balance;

import lombok.RequiredArgsConstructor;
import org.chomookun.fintics.core.asset.repository.AssetRepository;
import org.chomookun.fintics.core.balance.model.*;
import org.chomookun.fintics.core.balance.repository.BalanceHistoryRepository;
import org.chomookun.fintics.core.balance.repository.DividendProfitRepository;
import org.chomookun.fintics.core.balance.repository.RealizedProfitRepository;
import org.chomookun.fintics.core.broker.BrokerService;
import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientFactory;
import org.chomookun.fintics.core.broker.model.Broker;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DividendProfitService {

    private final DividendProfitRepository dividendProfitRepository;

    public List<DividendProfit> getDividendProfits(String brokerId, LocalDate dateFrom, LocalDate dateTo) {
        return dividendProfitRepository.findAllByBrokerId(brokerId, dateFrom, dateTo).stream()
                .map(DividendProfit::from)
                .toList();
    }

}
