package org.chomookun.fintics.client.asset;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;
import java.util.Optional;

@ConfigurationProperties(prefix = "fintics.asset-client")
@AllArgsConstructor
@Getter
public class AssetClientProperties {

    private final String className;

    private Map<String, String> properties;

    public Optional<String> getProperty(String name) {
        return Optional.ofNullable(properties.get(name));
    }

}
