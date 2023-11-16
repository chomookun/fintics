package org.oopscraft.fintics.client.trade.upbit;

import org.oopscraft.fintics.client.trade.TradeClient;
import org.oopscraft.fintics.client.trade.TradeClientDefinition;
import org.oopscraft.fintics.client.trade.kis.KisTradeClient;
import org.springframework.stereotype.Component;

import java.util.StringJoiner;

@Component
public class UpbitTradeClientDefinition implements TradeClientDefinition {

    @Override
    public Class<? extends TradeClient> getType() {
        return KisTradeClient.class;
    }

    @Override
    public String getName() {
        return "업비트 API";
    }

    @Override
    public String getPropertiesTemplate() {
        StringJoiner template = new StringJoiner("\n");
        template.add("accessKey=[발급 accessKey]");
        template.add("secretKey=[발급 secretKey]");
        return template.toString();
    }

}
