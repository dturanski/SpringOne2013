package org.springframework.xd.demo.gemfire;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ImportResource;

@ComponentScan
@EnableAutoConfiguration
@ImportResource("/META-INF/spring/client-cache.xml")
public class Application {

    public static void main(String[] args) {
       SpringApplication.run(Application.class, args);
    }
}
