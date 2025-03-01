package org.chomookun.fintics.core.basket.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.chomookun.arch4j.core.common.data.converter.GenericEnumConverter;
import org.chomookun.arch4j.core.common.data.converter.BooleanConverter;
import org.chomookun.fintics.core.basket.model.Basket;

import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fintics_basket")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketEntity extends BaseEntity {

    @Id
    @Column(name = "basket_id")
    @Comment("Basket ID")
    private String basketId;

    @Column(name = "name")
    @Comment("Name")
    private String name;

    @Column(name = "market", length = 16)
    @Comment("Market")
    private String market;

    @Column(name = "rebalance_enabled", length = 1)
    @Convert(converter = BooleanConverter.class)
    @Comment("Rebalance Enabled")
    private boolean rebalanceEnabled;

    @Column(name = "rebalance_schedule")
    @Comment("Rebalance Schedule")
    private String rebalanceSchedule;

    @Column(name = "language", length = 16)
    @Convert(converter = LanguageConverter.class)
    @Comment("Language")
    private Basket.Language language;

    @Column(name = "variables")
    @Lob
    private String variables;

    @Column(name = "script")
    @Lob
    private String script;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "basket_id", updatable = false)
    @OrderBy(BasketAssetEntity_.SORT)
    @Builder.Default
    @Setter(AccessLevel.NONE)
    private List<BasketAssetEntity> basketAssets = new ArrayList<>();

    /**
     * Language converter
     */
    public static class LanguageConverter extends GenericEnumConverter<Basket.Language> {}

}
