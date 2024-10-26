package org.oopscraft.fintics.dao;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.oopscraft.arch4j.core.common.data.BaseEntity;
import org.oopscraft.arch4j.core.common.data.converter.BooleanConverter;
import org.oopscraft.fintics.model.Order;

import javax.persistence.*;
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

    @Column(name = "alarm_id", length = 32)
    private String alarmId;

    @Column(name = "alarm_on_error", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean alarmOnError;

    @Column(name = "alarm_on_order", length = 1)
    @Convert(converter = BooleanConverter.class)
    private boolean alarmOnOrder;

}
