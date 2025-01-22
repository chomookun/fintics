package org.chomoo.fintics.client.broker;

import org.springframework.beans.factory.Aware;

import java.time.ZoneId;
import java.util.Currency;

/**
 * BrokerClientDefinition interface
 */
public interface BrokerClientDefinition extends Aware {

    /**
     * defines broker client id
     * @return client id
     */
    String getBrokerClientId();

    /**
     * defines broker client name
     * @return client name
     */
    String getBrokerClientName();

    /**
     * defines client class type
     * @return client class type
     */
    Class<? extends BrokerClient> getClassType();

    /**
     * defines client properties template
     * @return properties template string
     */
    String getPropertiesTemplate();

    /**
     * define market
     * @return market code
     */
    String getMarket();

    /**
     * define market time-zone
     * @return time-zone zoneId
     */
    ZoneId getTimezone();

    /**
     * defines currency unit
     * @return currency
     */
    Currency getCurrency();

}
