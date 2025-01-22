package org.chomoo.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomoo.arch4j.core.common.data.BaseEntity;
import org.chomoo.fintics.model.Order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(
        name = "fintics_order",
        indexes = {
                @Index(name = "ix_fintics_order_order_at", columnList = "order_at"),
                @Index(name = "ix_fintics_order_trade_id", columnList = "trade_id"),
                @Index(name = "ix_fintics_order_asset_id", columnList = "asset_id"),
                @Index(name = "ix_fintics_order_asset_name", columnList = "asset_name")
        }
)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderEntity extends BaseEntity {

    @Id
    @Column(name = "order_id", length = 32)
    private String orderId;

    @Column(name = "order_at")
    private Instant orderAt;

    @Column(name = "type", length = 8)
    private Order.Type type;

    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Column(name = "asset_name")
    private String assetName;

    @Column(name = "kind", length = 16)
    private Order.Kind kind;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "price", scale = 4)
    private BigDecimal price;

    @Column(name = "strategy_result_data")
    @Lob
    private String strategyResultData;

    @Column(name = "purchase_price", scale = 4)
    private BigDecimal purchasePrice;

    @Column(name = "realized_profit_amount", scale = 4)
    private BigDecimal realizedProfitAmount;

    @Column(name = "broker_order_id", length = 128)
    private String brokerOrderId;

    @Column(name = "result", length = 16)
    private Order.Result result;

    @Column(name = "error_message")
    @Lob
    private String errorMessage;

}
