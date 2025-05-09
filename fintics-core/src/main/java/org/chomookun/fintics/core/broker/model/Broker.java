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

    private Integer sort;

    private String clientType;

    private String clientProperties;

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
                .sort(brokerEntity.getSort())
                .clientType(brokerEntity.getClientType())
                .clientProperties(brokerEntity.getClientProperties())
                .build();
    }

}
