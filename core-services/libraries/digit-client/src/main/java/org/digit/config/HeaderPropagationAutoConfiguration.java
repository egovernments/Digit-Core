package org.digit.config;

import org.digit.exception.DigitClientErrorHandler;
import org.digit.services.account.AccountClient;
import org.digit.services.billing.BillingClient;
import org.digit.services.boundary.BoundaryClient;
import org.digit.services.employee.EmployeeClient;
import org.digit.services.filestore.FilestoreClient;
import org.digit.services.idgen.IdGenClient;
import org.digit.services.individual.IndividualClient;
import org.digit.services.mdms.MdmsClient;
import org.digit.services.notification.NotificationClient;
import org.digit.services.otp.OtpClient;
import org.digit.services.registry.RegistryClient;
import org.digit.services.workflow.WorkflowClient;
import org.digit.util.DigitJson;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.json.JacksonJsonHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@AutoConfiguration
@ConditionalOnClass(value={RestTemplate.class})
public class HeaderPropagationAutoConfiguration {


    /**
     * The RestTemplate the clients call through. Boot does not auto-configure a {@code RestTemplate}
     * (only a builder), so without this every client bean fails to resolve one; and a consumer's own
     * template would carry neither our converter nor our error handler. Injected by name so this and
     * a consumer's template can coexist.
     */
    @Bean("digitRestTemplate")
    @ConditionalOnMissingBean(name="digitRestTemplate")
    public RestTemplate digitRestTemplate(ApiProperties apiProperties,
                                          ClientHttpRequestInterceptor headerPropagationInterceptor) {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(requestFactory(apiProperties.getConnectTimeout(), apiProperties.getReadTimeout()));
        restTemplate.getMessageConverters().add(0, new JacksonJsonHttpMessageConverter(DigitJson.mapper()));
        restTemplate.setErrorHandler(new DigitClientErrorHandler());
        restTemplate.setInterceptors(List.of(headerPropagationInterceptor));
        return restTemplate;
    }

    /**
     * Uses the JDK HttpClient factory rather than {@code SimpleClientHttpRequestFactory}, which
     * enables output only for POST, PUT, PATCH and DELETE and therefore cannot send a body on a GET.
     * Several services expose canonical (envelope) read endpoints as GET-with-body, so those are
     * unreachable with the simple factory.
     *
     * <p>The connect timeout belongs to the underlying {@code HttpClient}; only the read timeout is
     * a property of the factory.
     */
    private static ClientHttpRequestFactory requestFactory(long connectTimeoutMillis, long readTimeoutMillis) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeoutMillis))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMillis));
        return factory;
    }

    @Bean
    @ConditionalOnMissingBean
    public ClientHttpRequestInterceptor headerPropagationInterceptor(PropagationProperties props) {
        return new HeaderPropagationInterceptor(props);
    }

    /**
     * Adds header propagation to RestTemplate beans this library did not create.
     *
     * <p>Off by default: it mutates every RestTemplate in the context, including ones pointed at
     * third-party hosts, which would send them the caller's {@code Authorization}, tenant and user
     * headers. Consumers who relied on the previous always-on behaviour can restore it with
     * {@code digit.propagate.auto-register-all-rest-templates=true}.
     */
    @Bean
    @ConditionalOnProperty(name="digit.propagate.auto-register-all-rest-templates", havingValue="true")
    public BeanPostProcessor restTemplateInterceptorProcessor(final ClientHttpRequestInterceptor headerPropagationInterceptor) {
        return new BeanPostProcessor(){

            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (!(bean instanceof RestTemplate restTemplate)) {
                    return bean;
                }
                // Reuse the interceptor bean and skip templates that already have one: a second
                // instance would resolve and apply the same headers twice.
                boolean alreadyPresent = restTemplate.getInterceptors().stream()
                        .anyMatch(HeaderPropagationInterceptor.class::isInstance);
                if (!alreadyPresent) {
                    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
                    interceptors.add(headerPropagationInterceptor);
                    restTemplate.setInterceptors(interceptors);
                }
                return bean;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={BoundaryClient.class})
    public BoundaryClient boundaryClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties) {
        return new BoundaryClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={WorkflowClient.class})
    public WorkflowClient workflowClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties) {
        return new WorkflowClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdGenClient idGenClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties) {
        return new IdGenClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public NotificationClient notificationClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties) {
        return new NotificationClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={IndividualClient.class})
    public IndividualClient individualClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties) {
        return new IndividualClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={FilestoreClient.class})
    public FilestoreClient filestoreClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties, PropagationProperties propagationProperties) {
        return new FilestoreClient(restTemplate, apiProperties, propagationProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={MdmsClient.class})
    public MdmsClient mdmsClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties) {
        return new MdmsClient(restTemplate, apiProperties);
    }

    /**
     * The Redis template is optional and supplied by {@code RegistryCacheAutoConfiguration}; without
     * it the client's cache paths are inert. Resolved through an {@link ObjectProvider} so a context
     * with no Redis still gets a working client rather than a missing-bean failure.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={RegistryClient.class})
    public RegistryClient registryClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties,
                                          @Qualifier("registryCacheRedisTemplate") ObjectProvider<RedisTemplate<String, String>> registryCacheTemplate) {
        return new RegistryClient(restTemplate, apiProperties, registryCacheTemplate.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={BillingClient.class})
    public BillingClient billingClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties) {
        return new BillingClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={AccountClient.class})
    public AccountClient accountClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties) {
        return new AccountClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={EmployeeClient.class})
    public EmployeeClient employeeClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties) {
        return new EmployeeClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={OtpClient.class})
    public OtpClient otpClient(@Qualifier("digitRestTemplate") RestTemplate restTemplate, ApiProperties apiProperties) {
        return new OtpClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix="digit.services")
    public ApiProperties apiProperties() {
        return new ApiProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public PropagationProperties propagationProperties() {
        return new PropagationProperties();
    }
}
