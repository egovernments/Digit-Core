package org.digit.config;

import org.digit.services.billing.BillingClient;
import org.digit.services.boundary.BoundaryClient;
import org.digit.services.filestore.FilestoreClient;
import org.digit.services.idgen.IdGenClient;
import org.digit.services.individual.IndividualClient;
import org.digit.services.mdms.MdmsClient;
import org.digit.services.notification.NotificationClient;
import org.digit.services.registry.RegistryClient;
import org.digit.services.workflow.WorkflowClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
@ConditionalOnClass(value={RestTemplate.class})
public class HeaderPropagationAutoConfiguration {
    @Bean
    @ConditionalOnMissingBean
    public ClientHttpRequestInterceptor headerPropagationInterceptor(PropagationProperties props) {
        return new HeaderPropagationInterceptor(props);
    }

    @Bean
    public BeanPostProcessor restTemplateInterceptorProcessor(final PropagationProperties propagationProperties) {
        return new BeanPostProcessor(){

            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof RestTemplate) {
                    RestTemplate restTemplate = (RestTemplate)bean;
                    HeaderPropagationInterceptor interceptor = new HeaderPropagationInterceptor(propagationProperties);
                    List<org.springframework.http.client.ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
                    interceptors.add(interceptor);
                    restTemplate.setInterceptors(interceptors);
                }
                return bean;
            }
        };
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={BoundaryClient.class})
    public BoundaryClient boundaryClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new BoundaryClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={WorkflowClient.class})
    public WorkflowClient workflowClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new WorkflowClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public IdGenClient idGenClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new IdGenClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public NotificationClient notificationClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new NotificationClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={IndividualClient.class})
    public IndividualClient individualClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new IndividualClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={FilestoreClient.class})
    public FilestoreClient filestoreClient(RestTemplate restTemplate, ApiProperties apiProperties, PropagationProperties propagationProperties) {
        return new FilestoreClient(restTemplate, apiProperties, propagationProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={MdmsClient.class})
    public MdmsClient mdmsClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new MdmsClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={RegistryClient.class})
    public RegistryClient registryClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new RegistryClient(restTemplate, apiProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(value={BillingClient.class})
    public BillingClient billingClient(RestTemplate restTemplate, ApiProperties apiProperties, ObjectMapper objectMapper) {
        return new BillingClient(restTemplate, apiProperties, objectMapper);
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

