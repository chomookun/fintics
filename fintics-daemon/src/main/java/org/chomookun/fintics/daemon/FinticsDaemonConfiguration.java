package org.chomookun.fintics.daemon;

import org.chomookun.arch4j.daemon.DaemonConfiguration;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({FinticsCoreConfiguration.class, DaemonConfiguration.class})
@EnableConfigurationProperties(FinticsDaemonProperties.class)
@EnableAutoConfiguration
public class FinticsDaemonConfiguration {

}