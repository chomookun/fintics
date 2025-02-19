package org.chomookun.fintics.core.broker.model;

import lombok.*;
import org.chomookun.fintics.core.broker.entity.BrokerEntity;

import java.time.ZoneId;
import java.util.Currency;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Broker {

    private String brokerId;

    private String name;

    private String brokerClientId;

    private String brokerClientProperties;

    private String market;

    private ZoneId timezone;

    private Currency currency;

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
