package org.chomookun.fintics.core.ohlcv.entity;

import lombok.*;
import org.chomookun.arch4j.core.common.data.converter.ZoneIdConverter;

import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "fintics_ohlcv_split")
@IdClass(OhlcvSplitEntity.Pk.class)
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@NoArgsConstructor
public class OhlcvSplitEntity {

    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    @Getter
    @NoArgsConstructor
    public static class Pk implements Serializable {
        private String assetId;
        private LocalDateTime dateTime;
    }

    @Id
    @Column(name = "asset_id")
    @Comment("Asset ID")
    private String assetId;

    @Id
    @Column(name = "date_time")
    @Comment("Date Time")
    private LocalDateTime dateTime;

    @Column(name = "time_zone")
    @Convert(converter = ZoneIdConverter.class)
    @Comment("Time Zone")
    private ZoneId timeZone;

    @Column(name = "split_from")
    @Comment("Split From")
    private BigDecimal splitFrom;

    @Column(name = "split_to")
    @Comment("Split To")
    private BigDecimal splitTo;

}
