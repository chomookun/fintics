package org.chomookun.fintics.core.asset.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.chomookun.arch4j.core.common.data.converter.BooleanConverter;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fintics_asset")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class AssetEntity extends BaseEntity {

    @Id
    @Column(name = "asset_id", length = 32)
    @Comment("Asset ID")
    private String assetId;

    @Column(name = "name")
    @Comment("Name")
    private String name;

    @Column(name = "market", length = 16)
    @Comment("Market")
    private String market;

    @Column(name = "exchange", length = 16)
    @Comment("Exchange")
    private String exchange;

    @Column(name = "type", length = 16)
    @Comment("Type")
    private String type;

    @Column(name = "favorite", length = 1)
    @Convert(converter = BooleanConverter.class)
    @Comment("Favorite")
    private boolean favorite;

    @Column(name = "updated_date")
    @Comment("Updated Date")
    private LocalDate updatedDate;

    @Column(name = "price", scale = 4)
    @Comment("Price")
    private BigDecimal price;

    @Column(name = "volume")
    @Comment("Volume")
    private BigDecimal volume;

    @Column(name = "market_cap", precision = 32)
    @Comment("Market Cap")
    private BigDecimal marketCap;

    @Column(name = "eps", scale = 2)
    @Comment("EPS")
    private BigDecimal eps;

    @Column(name = "roe", scale = 2)
    @Comment("ROE")
    private BigDecimal roe;

    @Column(name = "per", scale = 2)
    @Comment("PER")
    private BigDecimal per;

    @Column(name = "dividend_frequency")
    @Comment("Dividend Frequency")
    private Integer dividendFrequency;

    @Column(name = "dividend_yield", scale = 2)
    @Comment("Dividend Yield")
    private BigDecimal dividendYield;

    @Column(name = "capital_gain", scale = 2)
    @Comment("Capital Gain")
    private BigDecimal capitalGain;

    @Column(name = "total_return", scale = 2)
    @Comment("Total Return")
    private BigDecimal totalReturn;

}
