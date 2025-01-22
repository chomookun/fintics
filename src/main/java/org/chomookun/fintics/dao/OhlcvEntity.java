package org.chomookun.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.chomookun.arch4j.core.common.data.converter.BooleanConverter;
import org.chomookun.arch4j.core.common.data.converter.ZoneIdConverter;
import org.chomookun.fintics.model.Ohlcv;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Entity
@Table(name = "fintics_ohlcv")
@IdClass(OhlcvEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OhlcvEntity extends BaseEntity {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private String assetId;
        private Ohlcv.Type type;
        private LocalDateTime dateTime;
    }

    @Id
    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Id
    @Column(name = "type", length = 16)
    @Enumerated(EnumType.STRING)
    private Ohlcv.Type type;

    @Id
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "time_zone")
    @Convert(converter = ZoneIdConverter.class)
    private ZoneId timeZone;

    @Column(name = "open", scale = 4)
    private BigDecimal open;

    @Column(name = "high", scale = 4)
    private BigDecimal high;

    @Column(name = "low", scale = 4)
    private BigDecimal low;

    @Column(name = "close", scale = 4)
    private BigDecimal close;

    @Column(name = "volume")
    private BigDecimal volume;

    @Column(name = "interpolated")
    @Convert(converter = BooleanConverter.class)
    private boolean interpolated;

}
