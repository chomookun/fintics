package org.chomookun.fintics.core.trade.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.chomookun.arch4j.core.common.data.converter.MapConverter;

import jakarta.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "fintics_trade_asset")
@IdClass(TradeAssetEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeAssetEntity extends BaseEntity {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private String tradeId;
        private String assetId;
    }

    @Id
    @Column(name = "trade_id")
    private String tradeId;

    @Id
    @Column(name = "asset_id")
    private String assetId;

    @Column(name = "previous_close", scale = 4)
    private BigDecimal previousClose;

    @Column(name = "open", scale = 4)
    private BigDecimal open;

    @Column(name = "close", scale = 4)
    private BigDecimal close;

    @Column(name = "message")
    @Lob
    private String message;

    @Column(name = "context")
    @Lob
    @Convert(converter = MapConverter.class)
    private Map<String, Object> context;

}
