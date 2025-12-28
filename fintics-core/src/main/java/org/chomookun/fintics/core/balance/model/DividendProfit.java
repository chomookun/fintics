package org.chomookun.fintics.core.balance.model;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.balance.entity.DividendProfitEntity;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class DividendProfit {

    private String brokerId;

    private LocalDate date;

    private String assetId;

    private String symbol;

    private String name;

    private LocalDate paymentDate;

    private BigDecimal holdingQuantity;

    private BigDecimal dividendAmount;

    private BigDecimal taxAmount;

    private BigDecimal netAmount;

    public static DividendProfit from(DividendProfitEntity dividendProfitEntity) {
        return DividendProfit.builder()
                .brokerId(dividendProfitEntity.getBrokerId())
                .date(dividendProfitEntity.getDate())
                .assetId(dividendProfitEntity.getAssetId())
                .symbol(dividendProfitEntity.getSymbol())
                .name(dividendProfitEntity.getName())
                .paymentDate(dividendProfitEntity.getPaymentDate())
                .holdingQuantity(dividendProfitEntity.getHoldingQuantity())
                .dividendAmount(dividendProfitEntity.getDividendAmount())
                .taxAmount(dividendProfitEntity.getTaxAmount())
                .netAmount(dividendProfitEntity.getNetAmount())
                .build();
    }

}
