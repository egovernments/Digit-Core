package org.egov;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@SpringBootApplication
@EnableAspectJAutoProxy
public class MDMSApplication {

    public static void main(String[] args) {
        SpringApplication.run(MDMSApplication.class, args);
    }

}
