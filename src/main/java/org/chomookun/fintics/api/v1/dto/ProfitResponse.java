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

    private BigDecimal profitAmount;

    private BigDecimal realizedProfitAmount;

    private BigDecimal dividendProfitAmount;

    @Builder.Default
    private List<RealizedProfitResponse> realizedProfits = new ArrayList<>();

    @Builder.Default
    private List<DividendProfitResponse> dividendProfits = new ArrayList<>();

    @Builder.Default
    private List<BalanceHistoryResponse> balanceHistories = new ArrayList<>();

    /**
     * factory method
     * @param profit profit
     * @return profit response
     */
    public static ProfitResponse from(Profit profit) {
        return ProfitResponse.builder()
                .brokerId(profit.getBrokerId())
                .profitAmount(profit.getProfitAmount())
                .realizedProfitAmount(profit.getRealizedProfitAmount())
                .dividendProfitAmount(profit.getDividendProfitAmount())
                .realizedProfits(profit.getRealizedProfits().stream()
                        .map(RealizedProfitResponse::from)
                        .toList())
                .dividendProfits(profit.getDividendProfits().stream()
                        .map(DividendProfitResponse::from)
                        .toList())
                .balanceHistories(profit.getBalanceHistories().stream()
                        .map(BalanceHistoryResponse::from)
                        .toList())
                .build();
    }

}
