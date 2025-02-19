package org.chomookun.fintics.core.strategy.runner;

import lombok.*;

import java.math.BigDecimal;

@Builder
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@ToString
public class StrategyResult {

    @EqualsAndHashCode.Include
    private Action action;

    private BigDecimal position;

    private String description;

    public enum Action {
        BUY, SELL
    }

    public static StrategyResult of(Action action, BigDecimal position, String description) {
        return StrategyResult.builder()
                .action(action)
                .position(position)
                .description(description)
                .build();
    }

}
