package org.chomookun.fintics.core.order.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.chomookun.arch4j.core.common.data.converter.GenericEnumConverter;
import org.chomookun.fintics.core.order.model.Order;

import jakarta.persistence.*;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "fintics_order",
        indexes = {
                @Index(name = "ix_fintics_order_order_at", columnList = "order_at"),
                @Index(name = "ix_fintics_order_trade_id", columnList = "trade_id"),
                @Index(name = "ix_fintics_order_asset_id", columnList = "asset_id"),
                @Index(name = "ix_fintics_order_asset_name", columnList = "asset_name")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class OrderEntity extends BaseEntity {

    @Id
    @Column(name = "order_id", length = 32)
    @Comment("Order ID")
    private String orderId;

    @Column(name = "order_at")
    @Comment("Order At")
    private Instant orderAt;

    @Column(name = "type", length = 8)
    @Comment("Type")
    private Order.Type type;

    @Column(name = "trade_id", length = 32)
    @Comment("Trade ID")
    private String tradeId;

    @Column(name = "asset_id", length = 32)
    @Comment("Asset ID")
    private String assetId;

    @Column(name = "asset_name")
    @Comment("Asset Name")
    private String assetName;

    @Column(name = "kind", length = 16)
    @Comment("Kind")
    private Order.Kind kind;

    @Column(name = "quantity")
    @Comment("Quantity")
    private BigDecimal quantity;

    @Column(name = "price", scale = 4)
    @Comment("Price")
    private BigDecimal price;

    @Column(name = "strategy_result_data")
    @Lob
    @Comment("Strategy Result Data")
    private String strategyResultData;

    @Column(name = "purchase_price", scale = 4)
    @Comment("Purchase Price")
    private BigDecimal purchasePrice;

    @Column(name = "realized_profit_amount", scale = 4)
    @Comment("Realized Profit Amount")
    private BigDecimal realizedProfitAmount;

    @Column(name = "result", length = 16)
    @Comment("Result")
    private Order.Result result;

    @Column(name = "broker_order_id", length = 128)
    @Comment("Broker Order ID")
    private String brokerOrderId;

    @Column(name = "error_message")
    @Lob
    @Comment("Error Message")
    private String errorMessage;

    @Converter(autoApply = true)
    public static class TypeConverter extends GenericEnumConverter<Order.Type> {}

    @Converter(autoApply = true)
    public static class KindConverter extends GenericEnumConverter<Order.Kind> {}

    @Converter(autoApply = true)
    public static class ResultConverter extends GenericEnumConverter<Order.Result> {}

}
