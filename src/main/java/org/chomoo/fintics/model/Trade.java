package org.chomoo.fintics.model;

import lombok.*;
import org.chomoo.fintics.dao.TradeEntity;

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

    private String alarmId;

    private boolean alarmOnError;

    private boolean alarmOnOrder;

    /**
     * from factory method
     * @param tradeEntity trade entity
     * @return trade
     */
    public static Trade from(TradeEntity tradeEntity) {
        return Trade.builder()
                .tradeId(tradeEntity.getTradeId())
                .name(tradeEntity.getName())
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
                .alarmId(tradeEntity.getAlarmId())
                .alarmOnError(tradeEntity.isAlarmOnError())
                .alarmOnOrder(tradeEntity.isAlarmOnOrder())
                .build();
    }

}
