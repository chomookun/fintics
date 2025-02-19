package org.chomookun.fintics.core.broker.client.kis;

import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinition;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Currency;
import java.util.StringJoiner;

@Component
public class KisUsBrokerClientDefinition implements BrokerClientDefinition {

    /**
     * gets broker id
     * @return broker id
     */
    @Override
    public String getBrokerClientId() {
        return "KIS_US";
    }

    /**
     * gets broker name
     * @return broker name
     */
    @Override
    public String getBrokerClientName() {
        return "Korea Investment Kis API (US Market)";
    }

    /**
     * gets broker client type
     * @return broker class type
     */
    @Override
    public Class<? extends BrokerClient> getClassType() {
        return KisUsBrokerClient.class;
    }

    /**
     * returns properties template string
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
     * gets broker market
     * @return broker market
     */
    @Override
    public String getMarket() {
        return "US";
    }

    /**
     * gets broker market time zone
     * @return market time zone
     */
    @Override
    public ZoneId getTimezone() {
        return ZoneId.of("America/New_York");
    }

    /**
     * returns currency unit - USD
     * @return USD currency
     */
    @Override
    public Currency getCurrency() {
        return Currency.getInstance("USD");
    }

}
