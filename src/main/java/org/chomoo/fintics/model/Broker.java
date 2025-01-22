package org.chomoo.fintics.model;

import lombok.*;
import org.chomoo.fintics.dao.BrokerEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Broker {

    private String brokerId;

    private String name;

    private String brokerClientId;

    private String brokerClientProperties;

    /**
     * from factory method
     * @param brokerEntity broker entity
     * @return broker
     */
    public static Broker from(BrokerEntity brokerEntity) {
        return Broker.builder()
                .brokerId(brokerEntity.getBrokerId())
                .name(brokerEntity.getName())
                .brokerClientId(brokerEntity.getBrokerClientId())
                .brokerClientProperties(brokerEntity.getBrokerClientProperties())
                .build();
    }

}
