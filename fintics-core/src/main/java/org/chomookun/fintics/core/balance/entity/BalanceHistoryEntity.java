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
@Table(name = "fintics_balance_history")
@IdClass(BalanceHistoryEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class BalanceHistoryEntity extends BaseEntity {

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pk implements Serializable {
        private String brokerId;
        private LocalDate date;
    }

    @Id
    @Column(name = "broker_id", length = 32)
    @Comment("Broker ID")
    private String brokerId;

    @Id
    @Column(name = "date")
    @Comment("Date")
    private LocalDate date;

    @Column(name = "total_amount", length = 4)
    @Comment("Total Amount")
    private BigDecimal totalAmount;

    @Column(name = "cash_amount", length = 4)
    @Comment("Cash Amount")
    private BigDecimal cashAmount;

    @Column(name = "purchase_amount", length = 4)
    @Comment("Purchase Amount")
    private BigDecimal purchaseAmount;

    @Column(name = "valuation_amount", length = 4)
    @Comment("Valuation Amount")
    private BigDecimal valuationAmount;

}
