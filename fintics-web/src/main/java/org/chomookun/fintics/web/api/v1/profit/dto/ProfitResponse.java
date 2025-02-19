package org.chomookun.fintics.web.api.v1.profit.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.profit.model.Profit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class ProfitResponse {

    private String brokerId;

    private BigDecimal totalAmount;

    private BigDecimal balanceProfitAmount;

    private BigDecimal balanceProfitPercentage;

    private BigDecimal realizedProfitAmount;

    private BigDecimal realizedProfitPercentage;

    private BigDecimal dividendProfitAmount;

    private BigDecimal dividendProfitPercentage;

    @Builder.Default
    private List<BalanceHistoryResponse> balanceHistories = new ArrayList<>();

    @Builder.Default
    private List<RealizedProfitResponse> realizedProfits = new ArrayList<>();

    @Builder.Default
    private List<DividendProfitResponse> dividendProfits = new ArrayList<>();

    /**
     * factory method
     * @param profit profit
     * @return profit response
     */
    public static ProfitResponse from(Profit profit) {
        return ProfitResponse.builder()
                .brokerId(profit.getBrokerId())
                .totalAmount(profit.getTotalAmount())
                .balanceProfitAmount(profit.getBalanceProfitAmount())
                .balanceProfitPercentage(profit.getBalanceProfitPercentage())
                .realizedProfitAmount(profit.getRealizedProfitAmount())
                .realizedProfitPercentage(profit.getRealizedProfitPercentage())
                .dividendProfitAmount(profit.getDividendProfitAmount())
                .dividendProfitPercentage(profit.getDividendProfitPercentage())
                .balanceHistories(profit.getBalanceHistories().stream()
                        .map(BalanceHistoryResponse::from)
                        .toList())
                .realizedProfits(profit.getRealizedProfits().stream()
                        .map(RealizedProfitResponse::from)
                        .toList())
                .dividendProfits(profit.getDividendProfits().stream()
                        .map(DividendProfitResponse::from)
                        .toList())
                .build();
    }

}
