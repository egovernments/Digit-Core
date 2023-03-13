package org.egov;

import org.cache2k.extra.spring.SpringCache2kCacheManager;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.egov.encryption.config.EncryptionConfiguration;
import org.egov.tracer.config.TracerConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;

import java.util.concurrent.TimeUnit;

@SpringBootApplication
@EnableAutoConfiguration
@EnableCaching
@Import({ TracerConfiguration.class, MultiStateInstanceUtil.class, EncryptionConfiguration.class   })
public class InboxApplication {

	public static void main(String[] args) {
		SpringApplication.run(InboxApplication.class, args);
	}

	@Value("${cache.expiry.minutes}")
	private int cacheExpiry;

	@Bean
	@Profile("!test")
	public CacheManager cacheManager(){
		return new SpringCache2kCacheManager().addCaches(b->b.name("businessServices").expireAfterWrite(cacheExpiry, TimeUnit.MINUTES)
				.entryCapacity(10)).addCaches(b->b.name("inboxConfiguration").expireAfterWrite(cacheExpiry, TimeUnit.MINUTES)
				.entryCapacity(10));
	}

}
