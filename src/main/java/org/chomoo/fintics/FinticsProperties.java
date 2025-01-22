package org.chomoo.fintics;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * fintics properties
 */
@ConfigurationProperties(prefix = "fintics")
@AllArgsConstructor
@Getter
public final class FinticsProperties {

    private final String systemAlarmId;

    private final Integer dataRetentionMonths;

}
