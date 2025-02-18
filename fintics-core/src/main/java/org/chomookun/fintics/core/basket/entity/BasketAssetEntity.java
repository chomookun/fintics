package org.chomookun.fintics.core.basket.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.chomookun.arch4j.core.common.data.converter.BooleanConverter;

import jakarta.persistence.*;
import org.chomookun.fintics.core.asset.entity.AssetEntity;

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
    private String basketId;

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "fixed", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean fixed;

    @Column(name = "enabled", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean enabled;

    @Column(name = "holding_weight", scale = 2)
    private BigDecimal holdingWeight;

    @Column(name = "variables")
    @Lob
    private String variables;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "asset_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private AssetEntity assetEntity;

}
