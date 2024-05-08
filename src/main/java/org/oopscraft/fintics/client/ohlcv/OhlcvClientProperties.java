package org.oopscraft.fintics.client.ohlcv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Map;
import java.util.Optional;

@ConfigurationProperties(prefix = "fintics.ohlcv-client")
@ConstructorBinding
@AllArgsConstructor
@Getter
@Builder
public class OhlcvClientProperties {

    private final Class<? extends OhlcvClient> className;

    private final Map<String, String> properties;

    public Optional<String> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

}
