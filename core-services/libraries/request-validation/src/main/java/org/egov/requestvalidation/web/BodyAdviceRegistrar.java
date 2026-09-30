package org.egov.requestvalidation.web;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/** Spring 6.1/6.2 append this advice before constructing RequestBody/HttpEntity resolvers. */
public final class BodyAdviceRegistrar implements BeanPostProcessor {
    private final ObjectProvider<StructuredBodyAdvice> advice;
    private final Set<RequestMappingHandlerAdapter> registered =
            Collections.newSetFromMap(new IdentityHashMap<>());

    public BodyAdviceRegistrar(ObjectProvider<StructuredBodyAdvice> advice) { this.advice = advice; }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        if (bean instanceof RequestMappingHandlerAdapter adapter) {
            synchronized (registered) {
                if (registered.add(adapter)) adapter.setRequestBodyAdvice(List.of(advice.getObject()));
            }
        }
        return bean;
    }
}
