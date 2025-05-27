package org.egov.tracer.annotations;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import org.egov.tracer.validators.HTMLValidator;

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
	public abstract Class<? extends Payload>[] payload() default {};
}