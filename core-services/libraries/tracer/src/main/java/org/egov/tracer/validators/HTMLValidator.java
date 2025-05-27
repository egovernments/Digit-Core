package org.egov.tracer.validators;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.egov.tracer.annotations.CustomSafeHtml;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.springframework.stereotype.Component;

@Component
public class HTMLValidator implements ConstraintValidator<CustomSafeHtml,String>
{
	@Override
	public void initialize(CustomSafeHtml constraintAnnotation) {
		ConstraintValidator.super.initialize(constraintAnnotation);
	}

	@Override
	public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
		if (s == null) return true; // handle null case based on your requirements
		return Jsoup.isValid(s, Safelist.basic());
	}
}