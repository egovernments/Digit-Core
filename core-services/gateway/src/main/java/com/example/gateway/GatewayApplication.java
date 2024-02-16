package com.example.gateway;
import com.example.gateway.filters.pre.AuthFilter;
import com.example.gateway.filters.pre.RequestStartTimeFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.List;

@SpringBootApplication
@RestController
public class GatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(GatewayApplication.class, args);
	}

	@Autowired
	private ObjectMapper objectMapper;

	@Value("${egov.user-info-header}")
	private String userInfoHeader;
	private List<String> encryptedUrlSet;
	private List<String> openEndpointsWhitelist;
	private List<String> mixedModeEndpointsWhitelist;

	@Value("${egov.encrypted-endpoints-list}")
	public void setEncrytpedUrlListValues(List<String> EcryptedListFromProperties) {
		this.encryptedUrlSet = Collections.unmodifiableList(EcryptedListFromProperties);
	}

	@Value("${egov.open-endpoints-whitelist}")
	public void setOpenEndpointsWhitelistValues(List<String> openUrlListFromProperties) {
		this.openEndpointsWhitelist = Collections.unmodifiableList(openUrlListFromProperties);
	}

	@Value("${egov.mixed-mode-endpoints-whitelist}")
	public void setMixModeEndpointListVaaues(List<String> mixModeUrlListFromProperties) {
		this.mixedModeEndpointsWhitelist = Collections.unmodifiableList(mixModeUrlListFromProperties);
	}

	@Value("${egov.auth-service-host}")
	private String authServiceHost;

	@Value("${egov.auth-service-uri}")
	private String authServiceUri;

	@Value("${egov.authorize.access.control.host}${egov.authorize.access.control.uri}")
	private String authorizationUrl;

	@Bean
	public GlobalFilter authFilter() {
		return new AuthFilter();
	}

	@Bean
	public GlobalFilter requestStartTimeFilter() {
		return new RequestStartTimeFilter();
	}

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}

//	@Autowired
//	private CustomRateLimitUtils customRateLimitUtils;

//	@Bean
//	public GlobalFilter correlationIdFilter() {
//		return new CorrelationIdFilter(openEndpointsWhitelist, mixedModeEndpointsWhitelist, this.objectMapper);
//	}
//
//	@Bean
//	public GlobalFilter authCheckFilter() {
//		return new AuthPreCheckFilter(openEndpointsWhitelist, mixedModeEndpointsWhitelist, userUtils,
//				multiStateInstanceUtil);
//	}
//
//	@Bean
//	public GlobalFilter authFilter() {
//		return new AuthFilter(restTemplate, authServiceHost, authServiceUri, multiStateInstanceUtil);
//	}
//
//	@Bean
//	public GlobalFilter rbacFilter() {
//		return new RbacFilter(restTemplate, authorizationUrl);
//	}
//
//	@Bean
//	public GlobalFilter rbacCheckFilter() {
//		return new RbacPreCheckFilter(openEndpointsWhitelist, mixedModeEndpointsWhitelist);
//	}

//	@Configuration
//	public static class RateLimitUtilsConfiguration {
//
//		@Bean
//		@ConditionalOnClass(name = "org.springframework.security.core.Authentication")
//		public RateLimitUtils securedRateLimitUtils(final RateLimitProperties rateLimitProperties) {
//			return new SecuredRateLimitUtils(rateLimitProperties);
//		}
//
//		@Bean
//		public RateLimitUtils rateLimitUtils(final RateLimitProperties rateLimitProperties) {
//			return new CustomRateLimitUtils(rateLimitProperties);
//		}
//	}

//	@Bean
//	public RouteLocator routeLocator(RouteLocatorBuilder builder) {
//		return builder.routes()
//				// Define your routes here using builder
//				.build();
//	}
}
