package org.egov.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Getter
public class OtpValidationConfig {

    @Value("${egov.mobile.validation.default.numeric.pattern}")
    private String defaultNumericPattern;

    @Value("${egov.mobile.validation.default.length.pattern}")
    private String defaultLengthPattern;
}
