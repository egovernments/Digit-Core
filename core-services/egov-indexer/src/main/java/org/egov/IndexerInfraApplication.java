package org.egov;


import org.cache2k.extra.spring.SpringCache2kCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.web.client.RestTemplate;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

import javax.net.ssl.*;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
@Configuration
@EnableCaching
@PropertySource("classpath:application.properties")
@EnableAspectJAutoProxy
public class IndexerInfraApplication {
    @Autowired
    private Environment env;

	@Value("${cache.expiry.mdms.masters.minutes}")
	private int mdmsMasterExpiry;

	public static void trustSelfSignedSSL() {
		try {
			SSLContext ctx = SSLContext.getInstance("TLS");
			X509TrustManager tm = new X509TrustManager() {

				public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};
			ctx.init(null, new TrustManager[]{tm}, null);
			SSLContext.setDefault(ctx);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public static void main(String[] args) {
		SpringApplication.run(IndexerInfraApplication.class, args);
	}    

	@Bean
	public RestTemplate restTemplate() {
		//trustSelfSignedSSL(); // This skips the ssl certificate verification for restTemplate calls made to es8
		return new RestTemplate();
	}

	@Bean
	@Profile("!test")
	public CacheManager cacheManager() {
		return new SpringCache2kCacheManager()
				.addCaches(b->b.name("masterData")
						.expireAfterWrite(mdmsMasterExpiry, TimeUnit.MINUTES));
	}
	
	
}
