package org.chomookun.fintics.core.broker.client.upbit;

import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Currency;
import java.util.StringJoiner;

@Component
@Lazy(false)
public class UpbitBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public String getBrokerClientId() {
        return "UPBIT";
    }

    @Override
    public String getBrokerClientName() {
        return "Upbit API";
    }

    @Override
    public Class<? extends BrokerClient> getClassType() {
        return UpbitBrokerClient.class;
    }

    @Override
    public String getPropertiesTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("accessKey=[Access Key]");
        template.add("secretKey=[Secret Key]");
        template.add("insecure=[true|false(default)]");
        return template.toString();
    }

    @Override
    public String getMarket() {
        return "UPBIT";
    }

    @Override
    public ZoneId getTimezone() {
        return ZoneId.of("Asia/Seoul");
    }

    @Override
    public Currency getCurrency() {
        return Currency.getInstance("KRW");
    }

}
