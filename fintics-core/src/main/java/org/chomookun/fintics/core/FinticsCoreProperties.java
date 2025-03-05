package org.chomookun.fintics.core;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Fintics core properties
 */
@ConfigurationProperties(prefix = "fintics.core")
@AllArgsConstructor
@Getter
public final class FinticsCoreProperties {

    @NotNull
    private final String systemAlarmId;

    @NotNull
    private final Integer dataRetentionMonths;

}
