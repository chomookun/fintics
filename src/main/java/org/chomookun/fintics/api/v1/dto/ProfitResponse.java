package org.chomookun.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.model.Profit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class ProfitResponse {

    private String brokerId;

    private BigDecimal totalAmount;

    private BigDecimal balanceProfitAmount;

    private BigDecimal realizedProfitAmount;

    private BigDecimal dividendProfitAmount;

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
                .realizedProfitAmount(profit.getRealizedProfitAmount())
                .dividendProfitAmount(profit.getDividendProfitAmount())
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
