package org.chomookun.fintics.web.api.v1.broker.dto;

import lombok.*;
import org.chomookun.fintics.core.broker.model.Broker;

import java.time.ZoneId;
import java.util.Currency;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class BrokerResponse {

    private String brokerId;

    private String name;

    private Integer sort;

    private String brokerClientId;

    private String brokerClientProperties;

    private String market;

    private ZoneId timezone;

    private Currency currency;

    public static BrokerResponse from(Broker broker) {
        return BrokerResponse.builder()
                .brokerId(broker.getBrokerId())
                .name(broker.getName())
                .sort(broker.getSort())
                .brokerClientId(broker.getBrokerClientId())
                .brokerClientProperties(broker.getBrokerClientProperties())
                .market(broker.getMarket())
                .timezone(broker.getTimezone())
                .currency(broker.getCurrency())
                .build();
    }

}
