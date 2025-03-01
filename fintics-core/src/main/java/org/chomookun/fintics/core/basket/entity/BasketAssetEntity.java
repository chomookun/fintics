package org.chomookun.fintics.core.basket.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.chomookun.arch4j.core.common.data.converter.BooleanConverter;

import jakarta.persistence.*;
import org.chomookun.fintics.core.asset.entity.AssetEntity;
import org.hibernate.annotations.Comment;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "fintics_basket_asset")
@IdClass(BasketAssetEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketAssetEntity extends BaseEntity {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private String basketId;
        private String assetId;
    }

    @Id
    @Column(name = "basket_id", length = 32)
    @Comment("Basket ID")
    private String basketId;

    @Id
    @Column(name = "asset_id", length = 32)
    @Comment("Asset ID")
    private String assetId;

    @Column(name = "sort")
    @Comment("Sort")
    private Integer sort;

    @Column(name = "fixed", length = 1)
    @Convert(converter = BooleanConverter.class)
    @Comment("Fixed")
    private boolean fixed;

    @Column(name = "enabled", length = 1)
    @Convert(converter = BooleanConverter.class)
    @Comment("Enabled")
    private boolean enabled;

    @Column(name = "holding_weight", scale = 2)
    @Comment("Holding Weight")
    private BigDecimal holdingWeight;

    @Column(name = "variables")
    @Lob
    @Comment("Variables")
    private String variables;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "asset_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private AssetEntity assetEntity;

}
