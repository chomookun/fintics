package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.common.data.BaseEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
    private String assetId;

    @Column(name = "name")
    private String name;

    @Column(name = "market", length = 16)
    private String market;

    @Column(name = "exchange", length = 16)
    private String exchange;

    @Column(name = "type", length = 16)
    private String type;

    @Column(name = "updated_date")
    private LocalDate updatedDate;

    @Column(name = "market_cap", precision = 32)
    private BigDecimal marketCap;

    @Column(name = "eps", scale = 2)
    private BigDecimal eps;

    @Column(name = "roe", scale = 2)
    private BigDecimal roe;

    @Column(name = "roa", scale = 2)
    private BigDecimal roa;

    @Column(name = "per", scale = 2)
    private BigDecimal per;

    @Column(name = "dividend_yield", scale = 2)
    private BigDecimal dividendYield;

    @Column(name = "dividend_frequency")
    private Integer dividendFrequency;

}
