package org.chomookun.fintics.core.ohlcv.client;

import lombok.Getter;
import org.chomookun.fintics.core.asset.model.Asset;
import org.chomookun.fintics.core.ohlcv.model.Ohlcv;

import java.time.LocalDateTime;
import java.util.List;

/**
 * abstract class of ohlcv client
 */
@Getter
public abstract class OhlcvClient {

    private final OhlcvClientProperties ohlcvClientProperties;

    /**
     * constructor
     * @param ohlcvClientProperties ohlcv client properties
     */
    public OhlcvClient(OhlcvClientProperties ohlcvClientProperties) {
        this.ohlcvClientProperties = ohlcvClientProperties;
    }

    /**
     * whether is supported
     * @param asset asset
     * @return support or not
     */
    public abstract boolean isSupported(Asset asset);

    public abstract List<Ohlcv> getOhlcvs(Asset asset, Ohlcv.Type type, LocalDateTime datetimeFrom, LocalDateTime datetimeTo);

}
