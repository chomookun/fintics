package org.chomookun.fintics.web.api.v1.trade.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.balance.model.DividendProfit;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DividendProfitResponse {

    private String assetId;

    private LocalDate date;

    private String symbol;

    private String name;

    private BigDecimal holdingQuantity;

    private BigDecimal dividendAmount;

    private BigDecimal taxAmount;

    private BigDecimal netAmount;

    private LocalDate paymentDate;

    public static DividendProfitResponse from(DividendProfit dividendProfit) {
        return DividendProfitResponse.builder()
                .date(dividendProfit.getDate())
                .symbol(dividendProfit.getSymbol())
                .name(dividendProfit.getName())
                .holdingQuantity(dividendProfit.getHoldingQuantity())
                .dividendAmount(dividendProfit.getDividendAmount())
                .taxAmount(dividendProfit.getTaxAmount())
                .netAmount(dividendProfit.getNetAmount())
                .paymentDate(dividendProfit.getPaymentDate())
                .build();
    }

}
