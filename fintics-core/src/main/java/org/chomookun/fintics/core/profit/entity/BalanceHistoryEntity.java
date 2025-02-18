package org.chomookun.fintics.core.profit.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;

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
    private String brokerId;

    @Id
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "total_amount", length = 4)
    private BigDecimal totalAmount;

    @Column(name = "cash_amount", length = 4)
    private BigDecimal cashAmount;

    @Column(name = "purchase_amount", length = 4)
    private BigDecimal purchaseAmount;

    @Column(name = "valuation_amount", length = 4)
    private BigDecimal valuationAmount;

}
