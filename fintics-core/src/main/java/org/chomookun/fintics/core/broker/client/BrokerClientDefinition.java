package org.chomookun.fintics.core.broker.client;

import org.springframework.beans.factory.Aware;

import java.time.ZoneId;
import java.util.Currency;

/**
 * BrokerClientDefinition interface
 */
public interface BrokerClientDefinition extends Aware {

    /**
     * Defines broker client id
     * @return client id
     */
    String getBrokerClientId();

    /**
     * Defines broker client name
     * @return client name
     */
    String getBrokerClientName();

    /**
     * Defines client class type
     * @return client class type
     */
    Class<? extends BrokerClient> getClassType();

    /**
     * Defines client properties template
     * @return properties template string
     */
    String getPropertiesTemplate();

    /**
     * Defines market
     * @return market code
     */
    String getMarket();

    /**
     * Defines market time-zone
     * @return time-zone zoneId
     */
    ZoneId getTimezone();

    /**
     * Defines currency unit
     * @return currency
     */
    Currency getCurrency();

}
