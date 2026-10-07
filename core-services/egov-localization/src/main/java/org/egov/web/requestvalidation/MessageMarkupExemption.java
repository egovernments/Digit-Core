package org.egov.web.requestvalidation;

import org.egov.requestvalidation.core.ContentExemption;
import org.egov.requestvalidation.core.FlaggedValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lets the configured message codes (HTML e-mail templates) carry plain formatting markup. Every other message,
 * and any markup that is not a safe template, is still rejected by request validation.
 */
@Component
public class MessageMarkupExemption implements ContentExemption {
    private final Set<String> codes;

    public MessageMarkupExemption(@Value("${egov.localization.markup.allowed-codes:}") List<String> codes) {
        this.codes = codes.stream().map(String::trim).filter(code -> !code.isEmpty()).collect(Collectors.toSet());
    }

    @Override
    public boolean allows(FlaggedValue value) {
        return value.matches("/messages/*/message")
                && value.sibling("code").map(codes::contains).orElse(false)
                && SafeHtmlTemplate.isSafe(value.value());
    }
}
