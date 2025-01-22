package org.chomoo.fintics;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.chomoo.arch4j.web.WebConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * fintics configuration
 */
@Configuration
@Import(WebConfiguration.class)
@ComponentScan(
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
@EnableAutoConfiguration
@EntityScan
@EnableJpaRepositories
@MapperScan(
        annotationClass = Mapper.class,
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
@ConfigurationPropertiesScan
@EnableScheduling
@EnableCaching
public class FinticsConfiguration {

}
