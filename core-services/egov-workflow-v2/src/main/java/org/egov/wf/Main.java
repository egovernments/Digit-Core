package org.egov.wf;


import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.cache2k.extra.spring.SpringCache2kCacheManager;
import org.egov.tracer.config.TracerConfiguration;
import org.egov.wf.config.WorkflowConfig;
import org.egov.wf.repository.ServiceRequestRepository;
import org.egov.wf.service.IndividualService;
import org.egov.wf.service.UserAdapter;
import org.egov.wf.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableCaching
@Import({TracerConfiguration.class})
@Slf4j
public class Main {

	@Value("${app.timezone}")
	private String timeZone;

	@Value("${cache.expiry.workflow.minutes}")
	private int workflowExpiry;

	public static void main(String[] args) throws Exception {
		SpringApplication.run(Main.class, args);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return new ObjectMapper()
				.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
				.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
				.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
				.setTimeZone(TimeZone.getTimeZone(timeZone));
	}

	@Bean
	@Primary
	public UserAdapter getUserAdapter(@Value("${egov.wf.user.adapter.qualifier}") String userAdapterQualifier,
									  @Autowired WorkflowConfig config,
									  @Autowired ServiceRequestRepository serviceRequestRepository,
									  @Autowired ObjectMapper mapper) {
		if (userAdapterQualifier.equalsIgnoreCase("individualService")) {
			log.info("Using individual module as user adapter");
			return new IndividualService(config, serviceRequestRepository, mapper);
		} else {
			log.info("Using egov-user module as user adapter");
			return new UserService(config, serviceRequestRepository, mapper);
		}
	}

	@Bean
	@Profile("!test")
	public CacheManager cacheManager() {
		return new SpringCache2kCacheManager().addCaches(b -> b.name("businessService").expireAfterWrite(workflowExpiry, TimeUnit.MINUTES)
				.entryCapacity(10)).addCaches(b -> b.name("roleTenantAndStatusesMapping").expireAfterWrite(workflowExpiry, TimeUnit.MINUTES)
				.entryCapacity(10));
	}

}
