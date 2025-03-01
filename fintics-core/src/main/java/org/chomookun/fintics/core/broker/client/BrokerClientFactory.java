package org.chomookun.fintics.core.broker.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.chomookun.arch4j.core.common.pbe.PbePropertiesUtil;
import org.chomookun.fintics.core.broker.model.Broker;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class BrokerClientFactory {

    private final BrokerClientDefinitionRegistry brokerClientDefinitionRegistry;

    /**
     * Gets broker client object
     * @param broker broker
     * @return broker client
     */
    public BrokerClient getObject(Broker broker) {
        BrokerClientDefinition brokerClientDefinition = brokerClientDefinitionRegistry.getBrokerClientDefinition(broker.getBrokerClientId()).orElseThrow();
        try {
            Class<? extends BrokerClient> clientClass = brokerClientDefinition.getClassType().asSubclass(BrokerClient.class);
            Constructor<? extends BrokerClient> constructor = clientClass.getConstructor(BrokerClientDefinition.class, Properties.class);
            Properties properties = PbePropertiesUtil.loadProperties(broker.getBrokerClientProperties());
            return constructor.newInstance(brokerClientDefinition, properties);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
