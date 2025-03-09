package org.chomookun.fintics.web.api.v1.strategy.dto;

import lombok.Builder;
import lombok.Getter;
import org.chomookun.fintics.core.strategy.model.Strategy;

@Builder
@Getter
public class StrategyResponse {

    private String strategyId;

    private String name;

    private Integer sort;

    private Strategy.Language language;

    private String variables;

    private String script;

    public static StrategyResponse from(Strategy strategy) {
        return StrategyResponse.builder()
                .strategyId(strategy.getStrategyId())
                .name(strategy.getName())
                .sort(strategy.getSort())
                .language(strategy.getLanguage())
                .variables(strategy.getVariables())
                .script(strategy.getScript())
                .build();
    }

}
