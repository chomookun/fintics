package org.chomookun.fintics.web.api.v1.trade.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.chomookun.fintics.core.order.model.Order;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "trade request")
public class TradeRequest {

    @Schema(description = "trade id", example = "test_trade")
    private String tradeId;

    @Schema(description = "name", example = "test trade")
    private String name;

    @Schema(description = "enabled", example = "false")
    private boolean enabled;

    @Schema(description = "interval(seconds)", example = "60")
    private Integer interval;

    @Schema(description = "threshold", example = "1")
    private Integer threshold;

    @Schema(description = "start at", example = "09:00")
    private LocalTime startAt;

    @Schema(description = "end at", example = "15:30")
    private LocalTime endAt;

    @Schema(description = "invest amount")
    private BigDecimal investAmount;

    @Schema(description = "order kind")
    private Order.Kind orderKind;

    @Schema(description = "cash asset id")
    private String cashAssetId;

    @Schema(description = "cash buffer weight")
    private BigDecimal cashBufferWeight;

    @Schema(description = "broker id")
    private String brokerId;

    @Schema(description = "basket id")
    private String basketId;

    @Schema(description = "strategy id")
    private String strategyId;

    @Schema(description = "strategy variables")
    private String strategyVariables;

    @Schema(description = "alarm id")
    private String alarmId;

    @Schema(description = "alarm on error", example = "false")
    private boolean alarmOnError;

    @Schema(description = "alarm on order", example = "false")
    private boolean alarmOnOrder;

}
