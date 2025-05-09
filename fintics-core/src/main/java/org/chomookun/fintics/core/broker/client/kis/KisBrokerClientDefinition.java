package org.chomookun.fintics.core.broker.client.kis;

import org.chomookun.fintics.core.broker.client.BrokerClient;
import org.chomookun.fintics.core.broker.client.BrokerClientDefinition;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.time.ZoneId;
import java.util.Currency;
import java.util.StringJoiner;

@Component
@Lazy(false)
public class KisBrokerClientDefinition implements BrokerClientDefinition {

    @Override
    public String getClientType() {
        return "KIS";
    }

    @Override
    public String getName() {
        return "Korea Investment Kis API";
    }

    @Override
    public Class<? extends BrokerClient> getClassType() {
        return KisBrokerClient.class;
    }

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

    @Override
    public String getMarket() {
        return "KR";
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
