package org.chomookun.fintics.core.broker.client.alpaca;

import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinition;
import org.chomookun.fintics.core.broker.client.kis.KisUsBrokerClient;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Currency;
import java.util.StringJoiner;

@Component
@Lazy(false)
public class AlpacaBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public String getBrokerClientId() {
        return "ALPACA";
    }

    @Override
    public String getBrokerClientName() {
        return "Alpaca API";
    }

    /**
     * Gets client class type
     * @return client class type
     */
    @Override
    public Class<? extends BrokerClient> getClassType() {
        return AlpacaBrokerClient.class;
    }

    /**
     * Get properties template
     * @return properties template
     */
    @Override
    public String getPropertiesTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("live=false");
        template.add("appSecret=[Application Secret]");
        template.add("accountNo=[Account Number]");
        template.add("insecure=[true|false(default)]");
        return template.toString();
    }

    @Override
    public String getMarket() {
        return "US";
    }

    @Override
    public ZoneId getTimezone() {
        return ZoneId.of("America/New_York");
    }

    @Override
    public Currency getCurrency() {
        return Currency.getInstance("USD");
    }

}
