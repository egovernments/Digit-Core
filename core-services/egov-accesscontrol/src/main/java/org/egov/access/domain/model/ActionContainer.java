package org.egov.access.domain.model;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Data
public class ActionContainer {

    private Set<String> uris;
    private Set<String> regexUris;

    // Compiled lazily on first use; volatile ensures visibility across threads.
    // Transient — rebuilt in-place from regexUris on first matchesRegexUri call.
    private transient volatile ConcurrentHashMap<String, Pattern> compiledPatterns;

    public ActionContainer() {
        this.uris = new HashSet<>();
        this.regexUris = new HashSet<>();
    }

    public boolean matchesRegexUri(String requestUri) {
        if (regexUris == null || regexUris.isEmpty()) return false;
        if (compiledPatterns == null) {
            compiledPatterns = new ConcurrentHashMap<>();
        }
        for (String regexUri : regexUris) {
            Pattern pattern = compiledPatterns.computeIfAbsent(regexUri,
                    u -> Pattern.compile(u.replaceAll("\\{\\w+\\}", "\\\\w+")));
            if (pattern.matcher(requestUri).matches()) return true;
        }
        return false;
    }
}
