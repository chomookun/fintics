package org.chomookun.fintics.core.asset.client.dividend;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.fintics.core.asset.client.dividend.market.KrDividendClient;
import org.chomookun.fintics.core.asset.client.dividend.market.UsDividendClient;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.asset.model.Dividend;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@ConditionalOnProperty(prefix = "fintics.core.asset.dividend-client", name = "class-name", havingValue = "org.chomookun.fintics.core.asset.client.dividend.DefaultDividendClient")
@Slf4j
public class DefaultDividendClient extends DividendClient{

    private final List<DividendClient> dividendClients = new ArrayList<>();

    /**
     * Constructor
     * @param dividendClientProperties dividend client properties
     */
    protected DefaultDividendClient(DividendClientProperties dividendClientProperties, ObjectMapper objectMapper) {
        super(dividendClientProperties);
        dividendClients.add(new UsDividendClient(dividendClientProperties, objectMapper));
        dividendClients.add(new KrDividendClient(dividendClientProperties));
    }

    /**
     * Checks support asset
     * @param asset asset
     * @return support or not
     */
    @Override
    public boolean isSupport(Asset asset) {
        for(DividendClient dividendClient : dividendClients) {
            if (dividendClient.isSupport(asset)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets dividends
     * @param asset asset
     * @param dateFrom date from
     * @param dateTo date to
     * @return dividends
     */
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
