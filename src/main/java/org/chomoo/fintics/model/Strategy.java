package org.chomoo.fintics.model;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomoo.arch4j.core.common.data.converter.AbstractEnumConverter;
import org.chomoo.fintics.dao.StrategyEntity;

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

    public static enum Language {
        GROOVY, PYTHON
    }

    @Converter(autoApply = true)
    public static class LanguageConverter extends AbstractEnumConverter<Language> {}

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
