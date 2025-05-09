package org.chomookun.fintics.core.trade.model;

import lombok.*;
import org.chomookun.fintics.core.trade.entity.TradeEntity;
import org.chomookun.fintics.core.order.model.Order;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Trade {

    private String tradeId;

    private String name;

    private Integer sort;

    private boolean enabled;

    private Integer interval;

    private Integer threshold;

    private ZoneId timezone;

    private LocalTime startTime;

    private LocalTime endTime;

    private BigDecimal investAmount;

    private Order.Kind orderKind;

    private String cashAssetId;

    private BigDecimal cashBufferWeight;

    private String brokerId;

    private String basketId;

    private String strategyId;

    private String strategyVariables;

    private String notifierId;

    private boolean notifyOnError;

    private boolean notifyOnOrder;

    public static Trade from(TradeEntity tradeEntity) {
        return Trade.builder()
                .tradeId(tradeEntity.getTradeId())
                .name(tradeEntity.getName())
                .sort(tradeEntity.getSort())
                .enabled(tradeEntity.isEnabled())
                .interval(tradeEntity.getInterval())
                .threshold(tradeEntity.getThreshold())
                .startTime(tradeEntity.getStartAt())
                .endTime(tradeEntity.getEndAt())
                .investAmount(tradeEntity.getInvestAmount())
                .orderKind(tradeEntity.getOrderKind())
                .cashAssetId(tradeEntity.getCashAssetId())
                .cashBufferWeight(tradeEntity.getCashBufferWeight())
                .brokerId(tradeEntity.getBrokerId())
                .basketId(tradeEntity.getBasketId())
                .strategyId(tradeEntity.getStrategyId())
                .strategyVariables(tradeEntity.getStrategyVariables())
                .notifierId(tradeEntity.getNotifierId())
                .notifyOnError(tradeEntity.isNotifyOnError())
                .notifyOnOrder(tradeEntity.isNotifyOnOrder())
                .build();
    }

}
