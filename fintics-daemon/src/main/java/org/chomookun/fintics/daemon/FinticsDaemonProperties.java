package org.chomookun.fintics.daemon;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * fintics properties
 */
@ConfigurationProperties(prefix = "fintics.daemon")
@AllArgsConstructor
@Getter
public final class FinticsDaemonProperties {

    private final Integer dataRetentionMonths;

}
