package org.egov.requestvalidation;

import org.egov.requestvalidation.core.ContentExemption;

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
    /**
     * Accepts body string values the content check flagged when this rule allows them, for fields that
     * legitimately hold markup. A bean of this type if one exists, otherwise created by the bean factory.
     * The innermost declaration wins. Like skipPaths, it needs a reason.
     */
    Class<? extends ContentExemption> exemption() default ContentExemption.None.class;
    // -1 inherits the enclosing declaration or service configuration.
    int maxBodyBytes() default -1;
    int maxDepth() default -1;
    int maxStringLength() default -1;
    int maxNameLength() default -1;
    long maxTokens() default -1;
    int maxNumberLength() default -1;
    int maxScalarLength() default -1;
}
