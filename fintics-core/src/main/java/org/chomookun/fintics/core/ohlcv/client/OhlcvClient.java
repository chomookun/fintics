package org.chomookun.fintics.core.ohlcv.client;

import lombok.Getter;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Abstract class of ohlcv client
 */
@Getter
public abstract class OhlcvClient {

    private final OhlcvClientProperties ohlcvClientProperties;

    /**
     * Constructor
     * @param ohlcvClientProperties ohlcv client properties
     */
    public OhlcvClient(OhlcvClientProperties ohlcvClientProperties) {
        this.ohlcvClientProperties = ohlcvClientProperties;
    }

    /**
     * Gets supported asset
     * @param asset asset
     * @return support or not
     */
    public abstract boolean isSupported(Asset asset);

    /**
     * Gets ohlcvs
     * @param asset asset
     * @param type type
     * @param dateTimeFrom date time from
     * @param dateTimeTo date time to
     * @return list of ohlcv
     */
    public abstract List<Ohlcv> getOhlcvs(Asset asset, Ohlcv.Type type, LocalDateTime dateTimeFrom, LocalDateTime dateTimeTo);

}
