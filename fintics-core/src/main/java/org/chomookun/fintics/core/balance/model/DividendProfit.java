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

    public static DividendProfit from(DividendProfitEntity entity) {
        return DividendProfit.builder()
                .brokerId(entity.getBrokerId())
                .date(entity.getDate())
                .assetId(entity.getAssetId())
                .symbol(entity.getSymbol())
                .name(entity.getName())
                .paymentDate(entity.getPaymentDate())
                .holdingQuantity(entity.getHoldingQuantity())
                .dividendAmount(entity.getDividendAmount())
                .build();
    }

}
