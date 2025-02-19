package org.chomookun.fintics.web;

import org.chomookun.arch4j.web.WebConfiguration;
import org.chomookun.fintics.core.FinticsCoreConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.Import;

@Configuration
@Import({FinticsCoreConfiguration.class, WebConfiguration.class})
@ComponentScan(
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
@EnableAutoConfiguration
public class FinticsWebConfiguration {

}
