package org.chomoo.fintics;

import org.chomoo.arch4j.core.common.support.SpringApplicationInstaller;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

import java.util.Arrays;

/**
 * fintics application
 */
@SpringBootApplication(
        nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class
)
public class FinticsApplication {

    /**
     * runs application
     * @param args arguments
     */
    public static void main(String[] args) {

        // install
        if(Arrays.asList(args).contains("install")) {
            SpringApplicationInstaller.install(FinticsApplication.class, args);
            System.exit(0);
        }

        // runs
        new SpringApplicationBuilder(FinticsApplication.class)
                .web(WebApplicationType.SERVLET)
                .registerShutdownHook(true)
                .run(args);
    }

}
