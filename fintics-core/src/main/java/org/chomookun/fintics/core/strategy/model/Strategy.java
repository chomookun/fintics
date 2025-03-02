package org.chomookun.fintics.core.strategy.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.converter.GenericEnumConverter;
import org.chomookun.fintics.core.strategy.entity.StrategyEntity;

import jakarta.persistence.Converter;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Strategy {

    private String strategyId;

    private String name;

    private Strategy.Language language;

    private String variables;

    private String script;

    /**
     * Strategy language
     */
    public enum Language {
        GROOVY, PYTHON
    }

    /**
     * Converts strategy entity to strategy
     * @param strategyEntity strategy entity
     * @return strategy
     */
    public static Strategy from(StrategyEntity strategyEntity) {
        return Strategy.builder()
                .strategyId(strategyEntity.getStrategyId())
                .name(strategyEntity.getName())
                .language(strategyEntity.getLanguage())
                .variables(strategyEntity.getVariables())
                .script(strategyEntity.getScript())
                .build();
    }

}
