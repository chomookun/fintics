package org.chomookun.fintics.core.dividend.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.hibernate.annotations.Comment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fintics_dividend")
@IdClass(DividendEntity.Pk.class)
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
public class DividendEntity extends BaseEntity {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private String assetId;
        private LocalDate date;
    }

    @Id
    @Column(name = "asset_id", length = 32)
    @Comment("Asset ID")
    private String assetId;

    @Id
    @Column(name = "date")
    @Comment("Date")
    private LocalDate date;

    @Column(name = "dividend_per_share", scale = 4)
    @Comment("Dividend per share")
    private BigDecimal dividendPerShare;

}
