package org.egov.requestvalidation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Opts a handler into validation. Body settings never switch off its scalar checks. */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.PARAMETER})
public @interface ValidateRequest {
    boolean enabled() default true;
    Structured structured() default Structured.DEFAULT;
    ValidationMode mode() default ValidationMode.DEFAULT;
    String[] skipPaths() default {};
    String reason() default "";
    // -1 inherits the enclosing declaration or service configuration.
    int maxBodyBytes() default -1;
    int maxDepth() default -1;
    int maxStringLength() default -1;
    int maxNameLength() default -1;
    long maxTokens() default -1;
    int maxNumberLength() default -1;
    int maxScalarLength() default -1;
}
