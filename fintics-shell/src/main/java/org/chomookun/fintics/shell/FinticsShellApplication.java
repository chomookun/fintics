package org.chomookun.fintics.shell;

import org.chomookun.arch4j.core.common.cli.SpringApplicationInstaller;
import org.chomookun.arch4j.shell.ShellApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.FullyQualifiedAnnotationBeanNameGenerator;

import java.util.Arrays;

/**
 * fintics shell application
 */
@SpringBootApplication(nameGenerator = FullyQualifiedAnnotationBeanNameGenerator.class)
public class FinticsShellApplication {

    /**
     * runs application
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