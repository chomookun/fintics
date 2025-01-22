package org.chomookun.fintics.client.broker;

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

    @Override
    public Object postProcessAfterInitialization(@NotNull Object bean, @NotNull String beanName) throws BeansException {
        if(bean instanceof BrokerClientDefinition) {
            brokerClientDefinitions.add((BrokerClientDefinition) bean);
        }
        return bean;
    }

    public List<BrokerClientDefinition> getBrokerClientDefinitions() {
        return brokerClientDefinitions;
    }

    public Optional<BrokerClientDefinition> getBrokerClientDefinition(String brokerClientId) {
        return brokerClientDefinitions.stream()
                .filter(it -> it.getBrokerClientId().equals(brokerClientId))
                .findFirst();
    }

}
