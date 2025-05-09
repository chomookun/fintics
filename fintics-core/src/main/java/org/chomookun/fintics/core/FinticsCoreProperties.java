package org.chomookun.fintics.core;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Lazy;

/**
 * Fintics core properties
 */
@ConfigurationProperties(prefix = "fintics.core")
@Lazy(false)
@AllArgsConstructor
@Getter
public final class FinticsCoreProperties {

    @NotNull
    private final String systemNotifierId;

    @NotNull
    private final Integer dataRetentionMonths;

}
