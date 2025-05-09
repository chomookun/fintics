package org.chomookun.fintics.core.broker.client;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BrokerClientDefinitionRegistry implements BeanPostProcessor {

    private final List<BrokerClientDefinition> brokerClientDefinitions = new ArrayList<>();

    /**
     * Post process before initialization
     * @param bean bean
     * @param beanName bean name
     * @return bean
     */
    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if(bean instanceof BrokerClientDefinition) {
            brokerClientDefinitions.add((BrokerClientDefinition) bean);
        }
        return bean;
    }

    /**
     * Gets broker client definitions
     * @return list of broker client definitions
     */
    public List<BrokerClientDefinition> getBrokerClientDefinitions() {
        return brokerClientDefinitions;
    }

    /**
     * Gets broker client definition
     * @param brokerClientId broker client id
     * @return broker client definition
     */
    public Optional<BrokerClientDefinition> getBrokerClientDefinition(String brokerClientId) {
        return brokerClientDefinitions.stream()
                .filter(it -> it.getClientType().equals(brokerClientId))
                .findFirst();
    }

}
