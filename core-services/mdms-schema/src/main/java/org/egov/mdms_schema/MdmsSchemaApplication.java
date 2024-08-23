package org.egov.mdms_schema;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan
@SpringBootApplication
public class MdmsSchemaApplication {

	public static void main(String[] args) {
		SpringApplication.run(MdmsSchemaApplication.class, args);
	}

}
