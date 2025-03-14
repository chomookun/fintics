package org.chomookun.fintics.core.broker.client.kis;

import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinition;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Currency;
import java.util.StringJoiner;

@Component
public class KisBrokerClientDefinition implements BrokerClientDefinition {

    /**
     * Gets broker client id
     * @return broker client id
     */
    @Override
    public String getBrokerClientId() {
        return "KIS";
    }

    /**
     * Gets broker client name
     * @return broker client name
     */
    @Override
    public String getBrokerClientName() {
        return "Korea Investment Kis API";
    }

    /**
     * Gets broker class type
     * @return broker class type
     */
    @Override
    public Class<? extends BrokerClient> getClassType() {
        return KisBrokerClient.class;
    }

    /**
     * Gets properties template
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
        template.add("insecure=[true|false(default)]");
        return template.toString();
    }

    /**
     * Returns market code - KR (south korea)
     * @return south korea market code
     */
    @Override
    public String getMarket() {
        return "KR";
    }

    /**
     * Returns market time zone - Asia/Seoul
     * @return market time zone
     */
    @Override
    public ZoneId getTimezone() {
        return ZoneId.of("Asia/Seoul");
    }

    /**
     * Returns currency - KRW
     * @return currency
     */
    @Override
    public Currency getCurrency() {
        return Currency.getInstance("KRW");
    }

}
