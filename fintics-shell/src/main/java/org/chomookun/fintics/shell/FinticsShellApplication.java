package org.chomookun.fintics.shell;

import org.chomookun.arch4j.shell.common.SpringApplicationInstaller;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

import java.util.Arrays;

/**
 * Fintics shell application
 */
@SpringBootApplication(nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class FinticsShellApplication {

    /**
     * Runs application
     * @param args arguments
     */
    public static void main(String[] args) {
        // install
        if(Arrays.asList(args).contains("install")) {
            SpringApplicationInstaller.install(FinticsShellApplication.class, args);
            System.exit(0);
        }
        // runs shell application
        new SpringApplicationBuilder(FinticsShellApplication.class)
                .web(WebApplicationType.NONE)
                .registerShutdownHook(true)
                .run(args);
    }

}