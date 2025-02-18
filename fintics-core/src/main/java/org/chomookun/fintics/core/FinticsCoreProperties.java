package org.chomookun.fintics.core;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * fintics properties
 */
@ConfigurationProperties(prefix = "fintics.core")
@AllArgsConstructor
@Getter
public final class FinticsCoreProperties {

    private final String systemAlarmId;

}
