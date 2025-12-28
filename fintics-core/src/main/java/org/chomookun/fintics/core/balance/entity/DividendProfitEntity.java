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
@Table(name = "fintics_dividend_profit")
@IdClass(DividendProfitEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class DividendProfitEntity extends BaseEntity {

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
    @Column(name= "logical_hash", length = 32)
    private String logicalHash;

    @Column(name = "date")
    @Comment("Date")
    private LocalDate date;

    @Column(name = "asset_id", length = 32)
    private String assetId;

    @Column(name = "symbol", length = 32)
    private String symbol;

    @Column(name = "name")
    private String name;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "holding_quantity")
    private BigDecimal holdingQuantity;

    @Column(name = "dividend_amount", scale = 4)
    private BigDecimal dividendAmount;

    @Column(name = "tax_amount", scale = 4)
    private BigDecimal taxAmount;

    @Column(name = "net_amount", scale = 4)
    private BigDecimal netAmount;


}
