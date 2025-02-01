package org.chomookun.fintics.client.dividend;

import lombok.Getter;
import org.chomookun.fintics.model.Asset;
import org.chomookun.fintics.model.Dividend;

import java.time.LocalDate;
import java.util.List;

@Getter
public abstract class DividendClient {

    private final DividendClientProperties dividendClientProperties;

    /**
     * constructor
     * @param dividendClientProperties dividend client properties
     */
    protected DividendClient(DividendClientProperties dividendClientProperties) {
        this.dividendClientProperties = dividendClientProperties;
    }

    /**
     * checks support asset
     * @param asset asset
     * @return support or not
     */
    public abstract boolean isSupport(Asset asset);

    /**
     * Gets dividends
     * @param asset asset
     * @param dateFrom date from
     * @param dateTo date to
     * @return dividends
     */
    public abstract List<Dividend> getDividends(Asset asset, LocalDate dateFrom, LocalDate dateTo);

}
