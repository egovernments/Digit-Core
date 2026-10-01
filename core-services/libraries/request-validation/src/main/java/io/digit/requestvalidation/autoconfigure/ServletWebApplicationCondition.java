package io.digit.requestvalidation.autoconfigure;

import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.web.context.ConfigurableWebEnvironment;
import org.springframework.web.context.WebApplicationContext;

/**
 * Matches servlet web applications, using the same checks as Boot's
 * {@code @ConditionalOnWebApplication(type = SERVLET)}. That attribute does not exist in Boot 1.5, and
 * without it Boot 2/3 would also match reactive applications.
 */
final class ServletWebApplicationCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (!ClassUtils.isPresent("org.springframework.web.context.support.GenericWebApplicationContext",
                context.getClassLoader())) {
            return false;
        }
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        if (beanFactory != null && ObjectUtils.containsElement(beanFactory.getRegisteredScopeNames(), "session")) {
            return true;
        }
        if (context.getEnvironment() instanceof ConfigurableWebEnvironment) {
            return true;
        }
        return context.getResourceLoader() instanceof WebApplicationContext;
    }
}
