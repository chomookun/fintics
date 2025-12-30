package org.chomookun.fintics.web.api.v1.trade.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.balance.model.ProfitSummary;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class ProfitSummaryResponse {

    private String brokerId;

    private BigDecimal totalAmount;

    private BigDecimal balanceProfitAmount;

    private BigDecimal balanceProfitPercentage;

    private BigDecimal realizedProfitAmount;

    private BigDecimal realizedProfitPercentage;

    private BigDecimal dividendProfitAmount;

    private BigDecimal dividendProfitPercentage;

    private BigDecimal dividendProfitTaxAmount;

    private BigDecimal dividendProfitTaxableAmount;

    private BigDecimal dividendProfitNetAmount;

    @Builder.Default
    private List<BalanceHistoryResponse> balanceHistories = new ArrayList<>();

    @Builder.Default
    private List<RealizedProfitResponse> realizedProfits = new ArrayList<>();

    @Builder.Default
    private List<DividendProfitResponse> dividendProfits = new ArrayList<>();

    /**
     * factory method
     * @param profitSummary profit
     * @return profit response
     */
    public static ProfitSummaryResponse from(ProfitSummary profitSummary) {
        return ProfitSummaryResponse.builder()
                .brokerId(profitSummary.getBrokerId())
                .totalAmount(profitSummary.getTotalAmount())
                .balanceProfitAmount(profitSummary.getBalanceProfitAmount())
                .balanceProfitPercentage(profitSummary.getBalanceProfitPercentage())
                .realizedProfitAmount(profitSummary.getRealizedProfitAmount())
                .realizedProfitPercentage(profitSummary.getRealizedProfitPercentage())
                .dividendProfitAmount(profitSummary.getDividendProfitAmount())
                .dividendProfitPercentage(profitSummary.getDividendProfitPercentage())
                .dividendProfitTaxAmount(profitSummary.getDividendProfitTaxAmount())
                .dividendProfitTaxableAmount(profitSummary.getDividendProfitTaxableAmount())
                .dividendProfitNetAmount(profitSummary.getDividendProfitNetAmount())
                .balanceHistories(profitSummary.getBalanceHistories().stream()
                        .map(BalanceHistoryResponse::from)
                        .toList())
                .realizedProfits(profitSummary.getRealizedProfits().stream()
                        .map(RealizedProfitResponse::from)
                        .toList())
                .dividendProfits(profitSummary.getDividendProfits().stream()
                        .map(DividendProfitResponse::from)
                        .toList())
                .build();
    }

}
