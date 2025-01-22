package org.chomoo.fintics.api.v1.dto;

import lombok.*;
import org.chomoo.fintics.model.Order;
import org.chomoo.fintics.model.Trade;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeResponse {

    private String tradeId;

    private String name;

    private boolean enabled;

    private Integer interval;

    private Integer threshold;

    private LocalTime startAt;

    private LocalTime endAt;

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

    public static TradeResponse from(Trade trade) {
        return TradeResponse.builder()
                .tradeId(trade.getTradeId())
                .name(trade.getName())
                .enabled(trade.isEnabled())
                .interval(trade.getInterval())
                .threshold(trade.getThreshold())
                .startAt(trade.getStartTime())
                .endAt(trade.getEndTime())
                .investAmount(trade.getInvestAmount())
                .orderKind(trade.getOrderKind())
                .cashAssetId(trade.getCashAssetId())
                .cashBufferWeight(trade.getCashBufferWeight())
                .brokerId(trade.getBrokerId())
                .basketId(trade.getBasketId())
                .strategyId(trade.getStrategyId())
                .strategyVariables(trade.getStrategyVariables())
                .alarmId(trade.getAlarmId())
                .alarmOnError(trade.isAlarmOnError())
                .alarmOnOrder(trade.isAlarmOnOrder())
                .build();
    }

}
