package org.egov.requestvalidation.web;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

public final class RequestValidationMvcConfigurer implements WebMvcConfigurer {
    private final ScalarParameterInterceptor interceptor;

    public RequestValidationMvcConfigurer(ScalarParameterInterceptor interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor);
    }
}
