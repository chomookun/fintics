package org.oopscraft.fintics.api.v1.dto;

import lombok.Builder;
import lombok.Getter;
import org.oopscraft.fintics.model.Profit;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class ProfitResponse {

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
