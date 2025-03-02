package org.chomookun.fintics.core.strategy.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.chomookun.arch4j.core.common.data.converter.GenericEnumConverter;
import org.chomookun.fintics.core.strategy.model.Strategy;

import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "fintics_strategy")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StrategyEntity extends BaseEntity {

    @Id
    @Column(name = "strategy_id", length = 32)
    @Comment("Strategy ID")
    private String strategyId;

    @Column(name = "name")
    @Comment("Name")
    private String name;

    @Column(name = "language", length = 16)
    @Convert(converter = LanguageConverter.class)
    @Comment("Language")
    private Strategy.Language language;

    @Column(name = "variables")
    @Lob
    @Comment("Variables")
    private String variables;

    @Column(name = "script")
    @Lob
    @Comment("Script")
    private String script;

    @Converter
    public static class LanguageConverter extends GenericEnumConverter<Strategy.Language> {}

}
