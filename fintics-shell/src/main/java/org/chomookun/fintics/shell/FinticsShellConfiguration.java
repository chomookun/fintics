package org.chomookun.fintics.shell;

import org.chomookun.arch4j.shell.ShellConfiguration;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({FinticsCoreConfiguration.class, ShellConfiguration.class})
@ConfigurationPropertiesScan
@EnableAutoConfiguration
public class FinticsShellConfiguration {

}