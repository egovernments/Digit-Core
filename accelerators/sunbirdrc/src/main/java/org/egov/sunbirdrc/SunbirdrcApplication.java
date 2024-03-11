package org.egov.sunbirdrc;

import org.egov.tracer.config.TracerConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
public class SunbirdrcApplication {

	public static void main(String[] args) {
		SpringApplication.run(SunbirdrcApplication.class, args);
	}
}

