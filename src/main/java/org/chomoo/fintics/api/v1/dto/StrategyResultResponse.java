package org.chomoo.fintics.api.v1.dto;

import lombok.*;
import org.chomoo.fintics.strategy.StrategyResult;

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
