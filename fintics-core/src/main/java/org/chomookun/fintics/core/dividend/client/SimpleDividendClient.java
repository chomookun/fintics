package org.chomookun.fintics.core.dividend.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.dividend.client.market.KrDividendClient;
import org.chomookun.fintics.core.dividend.client.market.UsDividendClient;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.dividend.model.Dividend;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "fintics.core.dividend.dividend-client", name = "class-name", havingValue="org.chomookun.fintics.core.dividend.client.SimpleDividendClient")
@Slf4j
public class SimpleDividendClient extends DividendClient{

    private final List<DividendClient> dividendClients = new ArrayList<>();

    /**
     * constructor
     * @param dividendClientProperties dividend client properties
     */
    protected SimpleDividendClient(DividendClientProperties dividendClientProperties, ObjectMapper objectMapper) {
        super(dividendClientProperties);
        dividendClients.add(new UsDividendClient(dividendClientProperties, objectMapper));
        dividendClients.add(new KrDividendClient(dividendClientProperties));
    }


    @Override
    public boolean isSupport(Asset asset) {
        for(DividendClient dividendClient : dividendClients) {
            if (dividendClient.isSupport(asset)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<Dividend> getDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo) {
        for(DividendClient dividendClient : dividendClients) {
            if (dividendClient.isSupport(asset)) {
                return dividendClient.getDividends(asset, dateFrom, dateTo);
            }
        }
        return List.of();
    }

}
