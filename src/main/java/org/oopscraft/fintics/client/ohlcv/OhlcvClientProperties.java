package org.oopscraft.fintics.client.asset;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;

import java.util.Map;
import java.util.Optional;

@ConfigurationProperties(prefix = "fintics.asset-ohlcv-client")
@ConstructorBinding
@AllArgsConstructor
@Getter
@Builder
public class AssetOhlcvClientProperties {

    private final Class<? extends AssetOhlcvClient> className;

    private final Map<String, String> properties;

    public Optional<String> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

}
