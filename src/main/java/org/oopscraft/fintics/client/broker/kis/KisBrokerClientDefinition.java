package org.oopscraft.fintics.client.broker.kis;

import org.oopscraft.fintics.client.broker.BrokerClient;
import org.oopscraft.fintics.client.broker.BrokerClientDefinition;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.StringJoiner;

@Component
public class KisBrokerClientDefinition implements BrokerClientDefinition {

    /**
     * gets broker client id
     * @return broker client id
     */
    @Override
    public String getBrokerClientId() {
        return "KIS";
    }

    /**
     * gets broker client name
     * @return broker client name
     */
    @Override
    public String getBrokerClientName() {
        return "Korea Investment Kis API";
    }

    /**
     * gets broker class type
     * @return broker class type
     */
    @Override
    public Class<? extends BrokerClient> getClassType() {
        return KisBrokerClient.class;
    }

    /**
     * gets properties template
     * @return properties template
     */
    @Override
    public String getPropertiesTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("production=true");
        template.add("apiUrl=https://openapi.koreainvestment.com:9443");
        template.add("appKey=[Application Key]");
        template.add("appSecret=[Application Secret]");
        template.add("accountNo=[Account Number]");
        return template.toString();
    }

    /**
     * returns market code - KR (south korea)
     * @return south korea market code
     */
    @Override
    public String getMarket() {
        return "KR";
    }

    /**
     * returns market time zone - Asia/Seoul
     * @return market time zone
     */
    @Override
    public ZoneId getTimezone() {
        return ZoneId.of("Asia/Seoul");
    }

}
