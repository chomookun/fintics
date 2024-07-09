package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.arch4j.core.data.converter.BooleanConverter;

import javax.persistence.*;
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

    @Column(name = "fixed")
    @Convert(converter = BooleanConverter.class)
    private boolean fixed;

    @Column(name = "enabled", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean enabled;

    @Column(name = "holding_weight")
    private BigDecimal holdingWeight;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "asset_id", insertable = false, updatable = false, foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private AssetEntity assetEntity;

}
