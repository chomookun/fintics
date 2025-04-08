package org.chomookun.fintics.web.api.v1.profit.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.broker.model.DividendProfit;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DividendProfitResponse {

    private String assetId;

    private String symbol;

    private String name;

    private LocalDate date;

    private LocalDate paymentDate;

    private BigDecimal holdingQuantity;

    private BigDecimal dividendAmount;

    public static DividendProfitResponse from(DividendProfit dividendHistory) {
        return DividendProfitResponse.builder()
                .date(dividendHistory.getDate())
                .symbol(dividendHistory.getSymbol())
                .name(dividendHistory.getName())
                .holdingQuantity(dividendHistory.getHoldingQuantity())
                .dividendAmount(dividendHistory.getDividendAmount())
                .paymentDate(dividendHistory.getPaymentDate())
                .build();
    }

}
