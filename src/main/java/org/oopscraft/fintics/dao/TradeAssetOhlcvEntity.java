package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.data.SystemFieldEntity;
import org.oopscraft.fintics.model.OhlcvType;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fintics_trade_asset_ohlcv")
@IdClass(TradeAssetOhlcvEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetOhlcvEntity extends SystemFieldEntity {

    public static class Pk implements Serializable {
        private String tradeId;
        private String symbol;
        private OhlcvType ohlcvType;
        private LocalDateTime dateTime;
    }

    @Id
    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Id
    @Column(name = "symbol", length = 32)
    private String symbol;

    @Id
    @Column(name = "ohlcv_type", length = 32)
    @Enumerated(EnumType.STRING)
    private OhlcvType ohlcvType;

    @Id
    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Column(name = "open_price")
    private BigDecimal openPrice;

    @Column(name = "high_price")
    private BigDecimal highPrice;

    @Column(name = "low_price")
    private BigDecimal lowPrice;

    @Column(name = "close_price")
    private BigDecimal closePrice;

    @Column(name = "volume")
    private BigDecimal volume;

}
