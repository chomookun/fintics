package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.BaseEntity;
import org.oopscraft.arch4j.core.data.converter.BooleanToYNConverter;
import org.oopscraft.fintics.model.Ohlcv;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class OhlcvEntity extends BaseEntity {

    @Id
    @Column(name = "type", length = 32)
    @Enumerated(EnumType.STRING)
    private Ohlcv.Type type;

    @Id
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "open_price", scale = 4)
    private BigDecimal openPrice;

    @Column(name = "high_price", scale = 4)
    private BigDecimal highPrice;

    @Column(name = "low_price", scale = 4)
    private BigDecimal lowPrice;

    @Column(name = "close_price", scale = 4)
    private BigDecimal closePrice;

    @Column(name = "volume")
    private BigDecimal volume;

}
