package com.digit.config;

import com.digit.services.account.AccountClient;
import com.digit.services.boundary.BoundaryClient;
import com.digit.services.workflow.WorkflowClient;
import com.digit.services.idgen.IdGenClient;
import com.digit.services.notification.NotificationClient;
import com.digit.services.individual.IndividualClient;
import com.digit.services.filestore.FilestoreClient;
import com.digit.services.mdms.MdmsClient;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Auto-configuration for header propagation in the digit-client library.
 */
@Configuration
@ConditionalOnClass(RestTemplate.class)
public class HeaderPropagationAutoConfiguration {

    /**
     * Creates the header propagation interceptor bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public ClientHttpRequestInterceptor headerPropagationInterceptor(PropagationProperties props) {
        return new HeaderPropagationInterceptor(props);
    }

    /**
     * BeanPostProcessor to automatically add header propagation interceptor to RestTemplate beans.
     */
    @Bean
    public BeanPostProcessor restTemplateInterceptorProcessor(PropagationProperties propagationProperties) {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
                if (bean instanceof RestTemplate) {
                    RestTemplate restTemplate = (RestTemplate) bean;
                    HeaderPropagationInterceptor interceptor = new HeaderPropagationInterceptor(propagationProperties);
                    List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>(restTemplate.getInterceptors());
                    interceptors.add(interceptor);
                    restTemplate.setInterceptors(interceptors);
                    System.out.println("✅ ADDED HeaderPropagationInterceptor to RestTemplate '" + beanName + "'! Total interceptors: " + interceptors.size());
                }
                return bean;
            }
        };
    }

    /**
     * Auto-configures BoundaryClient bean if not already present.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(BoundaryClient.class)
    public BoundaryClient boundaryClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new BoundaryClient(restTemplate, apiProperties);
    }

    /**
     * Auto-configures AccountClient bean if not already present.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(AccountClient.class)
    public AccountClient accountClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new AccountClient(restTemplate, apiProperties);
    }

    /**
     * Auto-configures WorkflowClient bean if not already present.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(WorkflowClient.class)
    public WorkflowClient workflowClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new WorkflowClient(restTemplate, apiProperties);
    }

    /**
     * Auto-configures IdGenClient bean if not already present.
     */
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

    /**
     * Auto-configures IndividualClient bean if not already present.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(IndividualClient.class)
    public IndividualClient individualClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new IndividualClient(restTemplate, apiProperties);
    }

    /**
     * Auto-configures FilestoreClient bean if not already present.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(FilestoreClient.class)
    public FilestoreClient filestoreClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new FilestoreClient(restTemplate, apiProperties);
    }

    /**
     * Auto-configures MdmsClient bean if not already present.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(MdmsClient.class)
    public MdmsClient mdmsClient(RestTemplate restTemplate, ApiProperties apiProperties) {
        return new MdmsClient(restTemplate, apiProperties);
    }

    /**
     * Auto-configures ApiProperties bean with configuration properties binding.
     */
    @Bean
    @ConditionalOnMissingBean
    @ConfigurationProperties(prefix = "digit.services")
    public ApiProperties apiProperties() {
        return new ApiProperties();
    }

    /**
     * Auto-configures PropagationProperties bean.
     */
    @Bean
    @ConditionalOnMissingBean
    public PropagationProperties propagationProperties() {
        return new PropagationProperties();
    }
}
