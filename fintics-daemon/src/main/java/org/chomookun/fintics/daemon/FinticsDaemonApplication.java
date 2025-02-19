package org.chomookun.fintics.daemon;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

/**
 * fintics trade application
 */
@SpringBootApplication(nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class FinticsDaemonApplication {

    /**
     * runs application
     * @param args arguments
     */
    public static void main(String[] args) {
        new SpringApplicationBuilder(FinticsDaemonApplication.class)
                .web(WebApplicationType.SERVLET)
                .registerShutdownHook(true)
                .run(args);
    }

}