package org.chomoo.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomoo.arch4j.core.common.data.BaseEntity;
import org.chomoo.arch4j.core.common.data.converter.BooleanConverter;
import org.chomoo.fintics.model.Basket;

import jakarta.persistence.*;
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
    private String basketId;

    @Column(name = "name")
    private String name;

    @Column(name = "market", length = 16)
    private String market;

    @Column(name = "rebalance_enabled", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean rebalanceEnabled;

    @Column(name = "rebalance_schedule")
    private String rebalanceSchedule;

    @Column(name = "language", length = 16)
    @Enumerated(EnumType.STRING)
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

}
