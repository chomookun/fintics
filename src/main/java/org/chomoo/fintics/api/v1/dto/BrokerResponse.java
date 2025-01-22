package org.chomoo.fintics.api.v1.dto;

import lombok.*;
import org.chomoo.fintics.model.Broker;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerResponse {

    private String brokerId;

    private String name;

    private String brokerClientId;

    private String brokerClientProperties;

    public static BrokerResponse from(Broker broker) {
        return BrokerResponse.builder()
                .brokerId(broker.getBrokerId())
                .name(broker.getName())
                .brokerClientId(broker.getBrokerClientId())
                .brokerClientProperties(broker.getBrokerClientProperties())
                .build();
    }

}
