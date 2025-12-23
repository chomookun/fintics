package org.chomookun.fintics.core.balance.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;
import org.hibernate.annotations.Comment;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fintics_realized_profit")
@IdClass(RealizedProfitEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RealizedProfitEntity extends BaseEntity {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private String brokerId;
        private String logicalHash;
    }

    @Id
    @Column(name = "broker_id", length = 32)
    @Comment("Broker ID")
    private String brokerId;

    @Id
    @Column(name = "logical_hash", length = 32)
    private String logicalHash;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "symbol", length = 32)
    private String symbol;

    @Column(name = "name")
    private String name;

    @Column(name = "quantity")
    private BigDecimal quantity;

    @Column(name = "purchase_price", scale = 4)
    private BigDecimal purchasePrice;

    @Column(name = "purchase_amount", scale = 4)
    private BigDecimal purchaseAmount;

    @Column(name = "dispose_price", scale = 4)
    private BigDecimal disposePrice;

    @Column(name = "dispose_amount", scale = 4)
    private BigDecimal disposeAmount;

    @Column(name = "fee_amount", scale = 4)
    private BigDecimal feeAmount;

    @Column(name = "profit_amount", scale = 4)
    private BigDecimal profitAmount;

    @Column(name= "profit_percentage", scale = 4)
    private BigDecimal profitPercentage;

}
