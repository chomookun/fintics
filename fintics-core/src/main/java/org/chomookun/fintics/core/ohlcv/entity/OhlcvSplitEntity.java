package org.chomookun.fintics.core.ohlcv.entity;

import lombok.*;
import org.chomookun.arch4j.core.common.data.converter.ZoneIdConverter;

import jakarta.persistence.*;
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
    private String assetId;

    @Id
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "time_zone")
    @Convert(converter = ZoneIdConverter.class)
    private ZoneId timeZone;

    @Column(name = "split_from")
    private BigDecimal splitFrom;

    @Column(name = "split_to")
    private BigDecimal splitTo;

}
