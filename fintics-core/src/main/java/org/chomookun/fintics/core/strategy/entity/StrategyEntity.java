package org.chomookun.fintics.core.strategy.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.chomookun.fintics.core.strategy.model.Strategy;

import jakarta.persistence.*;

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
    private String strategyId;

    @Column(name = "name")
    private String name;

    @Column(name = "language", length = 16)
    private Strategy.Language language;

    @Column(name = "variables")
    @Lob
    private String variables;

    @Column(name = "script")
    @Lob
    private String script;

}
