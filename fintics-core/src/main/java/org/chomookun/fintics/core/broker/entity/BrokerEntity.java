package org.chomookun.fintics.core.broker.entity;

import lombok.*;
import lombok.experimental.SuperBuilder;
import org.chomookun.arch4j.core.common.data.BaseEntity;

import jakarta.persistence.*;

@Entity
@Table(name = "fintics_broker")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerEntity extends BaseEntity {

    @Id
    @Column(name = "broker_id", length = 32)
    private String brokerId;

    @Column(name = "name")
    private String name;

    @Column(name = "sort")
    private Integer sort;

    @Column(name = "broker_client_id", length = 32)
    private String brokerClientId;

    @Column(name = "broker_client_properties")
    @Lob
    private String brokerClientProperties;

}
