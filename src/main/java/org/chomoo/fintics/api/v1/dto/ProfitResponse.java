package org.chomoo.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomoo.fintics.model.Profit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class ProfitResponse {

    private BigDecimal totalAmount;

    private BigDecimal realizedProfitAmount;

    private BigDecimal dividendAmount;

    @Builder.Default
    private List<RealizedProfitResponse> realizedProfits = new ArrayList<>();

    @Builder.Default
    private List<DividendHistoryResponse> dividendHistories = new ArrayList<>();

    /**
     * factory method
     * @param profit profit
     * @return profit response
     */
    public static ProfitResponse from(Profit profit) {
        return ProfitResponse.builder()
                .totalAmount(profit.getTotalAmount())
                .realizedProfitAmount(profit.getRealizedProfitAmount())
                .dividendAmount(profit.getDividendAmount())
                .realizedProfits(profit.getRealizedProfits().stream()
                        .map(RealizedProfitResponse::from)
                        .toList())
                .dividendHistories(profit.getDividendHistories().stream()
                        .map(DividendHistoryResponse::from)
                        .toList())
                .build();
    }

}
