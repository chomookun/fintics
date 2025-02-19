package org.chomookun.fintics.core.ohlcv.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Optional;

/**
 * ohlcv client properties
 */
@ConfigurationProperties(prefix = "fintics.core.ohlcv.ohlcv-client")
@AllArgsConstructor
@Getter
@Builder
public class OhlcvClientProperties {

    private final String className;

    private final Map<String, String> properties;

    /**
     * gets property by name
     * @param name property name
     * @return property value
     */
    public Optional<String> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

}
