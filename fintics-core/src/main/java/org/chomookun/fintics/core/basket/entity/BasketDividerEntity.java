package org.chomookun.fintics.core.basket.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;

import java.io.Serializable;

@Entity
@Table(name = "fintics_basket_divider")
@IdClass(BasketDividerEntity.Pk.class)
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BasketDividerEntity extends BaseEntity {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Pk implements Serializable {
        private String basketId;
        private String dividerId;
    }

    @Id
    @Column(name = "basket_id", length = 32)
    private String basketId;

    @Id
    @Column(name = "divider_id", length = 32)
    private String dividerId;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "name", length = 4000)
    private String name;

}
