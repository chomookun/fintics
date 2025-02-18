package org.chomookun.fintics.web;

import org.chomookun.arch4j.web.WebConfiguration;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import({FinticsCoreConfiguration.class, WebConfiguration.class})
public class FinticsWebConfiguration {

}
