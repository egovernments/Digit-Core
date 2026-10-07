package org.egov.infra.mdms.requestvalidation;

import org.egov.requestvalidation.core.ContentExemption;
import org.egov.requestvalidation.core.FlaggedValue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Lets master data carry the markup some masters legitimately hold: HTML e-mail templates in the configured
 * schema fields, and known type names such as {@code List<String>} in {@code formDataType}. Everything else,
 * and any markup that is not a safe template or a listed type name, is still rejected by request validation.
 */
@Component
public class MdmsMarkupExemption implements ContentExemption {
    private static final String FORM_DATA_TYPE = "formDataType";

    private final Map<String, List<String>> htmlFields = new HashMap<>();
    private final Set<String> typeNames;

    /**
     * @param htmlFields {@code schemaCode:/Mdms/data/...} pairs; a {@code *} pointer segment matches one segment
     * @param typeNames exact formDataType values to accept
     */
    public MdmsMarkupExemption(@Value("${egov.mdms.markup.html-fields:}") List<String> htmlFields,
                               @Value("${egov.mdms.markup.type-names:}") List<String> typeNames) {
        for (String entry : htmlFields) {
            int separator = entry.indexOf(':');
            if (entry.isBlank()) continue;
            if (separator <= 0 || !entry.substring(separator + 1).trim().startsWith("/Mdms/data/")) {
                throw new IllegalArgumentException("egov.mdms.markup.html-fields entries must be schemaCode:/Mdms/data/...");
            }
            this.htmlFields.computeIfAbsent(entry.substring(0, separator).trim(), schema -> new ArrayList<>())
                    .add(entry.substring(separator + 1).trim());
        }
        this.typeNames = typeNames.stream().map(String::trim).filter(name -> !name.isEmpty()).collect(Collectors.toSet());
    }

    @Override
    public boolean allows(FlaggedValue value) {
        List<String> path = value.path();
        if (path.size() > 2 && "Mdms".equals(path.get(0)) && "data".equals(path.get(1))
                && FORM_DATA_TYPE.equals(path.get(path.size() - 1))) {
            return typeNames.contains(value.value());
        }
        List<String> fields = value.string("/Mdms/schemaCode").map(htmlFields::get).orElse(null);
        return fields != null && fields.stream().anyMatch(value::matches) && SafeHtmlTemplate.isSafe(value.value());
    }
}
