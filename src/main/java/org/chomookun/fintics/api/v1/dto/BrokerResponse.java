package org.chomookun.fintics.api.v1.dto;

import lombok.*;
import org.chomookun.fintics.model.Broker;

import java.time.ZoneId;
import java.util.Currency;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerResponse {

    private String brokerId;

    private String name;

    private String brokerClientId;

    private String brokerClientProperties;

    private String market;

    private ZoneId timezone;

    private Currency currency;

    public static BrokerResponse from(Broker broker) {
        return BrokerResponse.builder()
                .brokerId(broker.getBrokerId())
                .name(broker.getName())
                .brokerClientId(broker.getBrokerClientId())
                .brokerClientProperties(broker.getBrokerClientProperties())
                .market(broker.getMarket())
                .timezone(broker.getTimezone())
                .currency(broker.getCurrency())
                .build();
    }

}
