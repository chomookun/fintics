package org.chomookun.fintics.core.trade.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.chomookun.arch4j.core.common.data.converter.BooleanConverter;
import org.chomookun.fintics.core.order.model.Order;

import jakarta.persistence.*;
import org.chomookun.fintics.core.trade.model.Trade;

import java.math.BigDecimal;
import java.time.LocalTime;

@Entity
@Table(name = "fintics_trade",
        indexes = {
        @Index(name = "idx_broker_id", columnList = "broker_id"),
        @Index(name = "idx_basket_id", columnList = "basket_id"),
        @Index(name = "idx_strategy_id", columnList = "strategy_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class TradeEntity extends BaseEntity {

    @Id
    @Column(name = "trade_id", length = 32)
    private String tradeId;

    @Column(name = "name")
    private String name;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "enabled", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean enabled;

    @Column(name = "interval")
    private Integer interval;

    @Column(name = "threshold")
    private Integer threshold;

    @Column(name = "start_at")
    private LocalTime startAt;

    @Column(name = "end_at")
    private LocalTime endAt;

    @Column(name = "invest_amount")
    private BigDecimal investAmount;

    @Column(name = "dca_enabled")
    @Convert(converter = BooleanConverter.class)
    private boolean dcaEnabled;

    @Column(name = "dca_frequency", length = 16)
    private Trade.DCA_FREQUENCY dcaFrequency;

    @Column(name = "dca_amount")
    private BigDecimal dcaAmount;

    @Column(name = "order_kind", length = 16)
    private Order.Kind orderKind;

    @Column(name = "cash_asset_id", length = 32)
    private String cashAssetId;

    @Column(name = "cash_buffer_weight", length = 2)
    private BigDecimal cashBufferWeight;

    @Column(name = "broker_id", length = 32)
    private String brokerId;

    @Column(name = "basket_id", length = 32)
    private String basketId;

    @Column(name = "strategy_id", length = 32)
    private String strategyId;

    @Column(name = "strategy_variables")
    @Lob
    private String strategyVariables;

    @Column(name = "notifier_id", length = 32)
    private String notifierId;

    @Column(name = "notify_on_error", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean notifyOnError;

    @Column(name = "notify_on_order", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean notifyOnOrder;

}
