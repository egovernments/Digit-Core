package org.egov.user.validators;

import org.egov.user.annotations.CustomSafeHtml;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class HTMLValidator implements ConstraintValidator<CustomSafeHtml, String> {

    @Override
    public void initialize(CustomSafeHtml constraintAnnotation) {
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        if (s == null) return true;
        return Jsoup.isValid(s, Safelist.basic());
    }
}
