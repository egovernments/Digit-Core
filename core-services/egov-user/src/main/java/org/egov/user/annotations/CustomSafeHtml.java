package org.egov.user.annotations;

import org.egov.user.validators.HTMLValidator;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = HTMLValidator.class)
public @interface CustomSafeHtml {
    String message() default "Unsafe HTML tags included";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
