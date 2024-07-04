package org.oopscraft.fintics.model;

import lombok.*;
import org.oopscraft.fintics.dao.TradeEntity;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Trade {

    private String tradeId;

    private String tradeName;

    private boolean enabled;

    private Integer interval;

    private Integer threshold;

    private ZoneId timezone;

    private LocalTime startTime;

    private LocalTime endTime;

    private BigDecimal investAmount;

    private String brokerId;

    private String strategyId;

    private String strategyVariables;

    private Order.Kind orderKind;

    private String alarmId;

    private boolean alarmOnError;

    private boolean alarmOnOrder;

    @Builder.Default
    private List<TradeAsset> tradeAssets = new ArrayList<>();

    public static Trade from(TradeEntity tradeEntity) {
        Trade trade = Trade.builder()
                .tradeId(tradeEntity.getTradeId())
                .tradeName(tradeEntity.getTradeName())
                .enabled(tradeEntity.isEnabled())
                .interval(tradeEntity.getInterval())
                .threshold(tradeEntity.getThreshold())
                .startTime(tradeEntity.getStartAt())
                .endTime(tradeEntity.getEndAt())
                .investAmount(tradeEntity.getInvestAmount())
                .brokerId(tradeEntity.getBrokerId())
                .strategyId(tradeEntity.getStrategyId())
                .strategyVariables(tradeEntity.getStrategyVariables())
                .orderKind(tradeEntity.getOrderKind())
                .alarmId(tradeEntity.getAlarmId())
                .alarmOnError(tradeEntity.isAlarmOnError())
                .alarmOnOrder(tradeEntity.isAlarmOnOrder())
                .build();

        // trade assets
        List<TradeAsset> tradeAssets = tradeEntity.getTradeAssets().stream()
                .map(TradeAsset::from)
                .collect(Collectors.toList());
        trade.setTradeAssets(tradeAssets);

        // return
        return trade;
    }

}
