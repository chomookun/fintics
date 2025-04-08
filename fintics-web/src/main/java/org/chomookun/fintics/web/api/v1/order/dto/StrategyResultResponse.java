package org.chomookun.fintics.web.api.v1.order.dto;

import lombok.*;
import org.chomookun.fintics.core.strategy.runner.StrategyResult;

import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StrategyResultResponse {

    private StrategyResult.Action action;

    private BigDecimal position;

    private String description;

    public static StrategyResultResponse from(StrategyResult strategyResult) {
        return StrategyResultResponse.builder()
                .action(strategyResult.getAction())
                .position(strategyResult.getPosition())
                .description(strategyResult.getDescription())
                .build();
    }

}
