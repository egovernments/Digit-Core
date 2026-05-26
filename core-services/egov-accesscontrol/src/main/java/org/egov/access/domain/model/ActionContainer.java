package org.egov.access.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Data
public class ActionContainer {

    private Set<String> uris;
    private Set<String> regexUris;

    // Regex URIs grouped by fixed prefix (text before the first {param}).
    // Each group is compiled into one alternation pattern.
    // At match time, startsWith(prefix) eliminates all irrelevant groups before
    // the regex engine runs, keeping effective alternation size ~10-30 per service
    // rather than the full N across all services.
    private transient volatile Map<String, Pattern> patternsByPrefix;

    public ActionContainer() {
        this.uris = new HashSet<>();
        this.regexUris = new HashSet<>();
    }

    public boolean matchesRegexUri(String requestUri) {
        if (regexUris == null || regexUris.isEmpty()) return false;
        if (patternsByPrefix == null) {
            synchronized (this) {
                if (patternsByPrefix == null)
                    patternsByPrefix = buildPatternsByPrefix();
            }
        }

        for (Map.Entry<String, Pattern> entry : patternsByPrefix.entrySet()) {
            if (requestUri.startsWith(entry.getKey())
                    && entry.getValue().matcher(requestUri).matches())
                return true;
        }
        return false;
    }

    private Map<String, Pattern> buildPatternsByPrefix() {
        Map<String, List<String>> grouped = new HashMap<>();
        for (String uri : regexUris) {
            int braceIdx = uri.indexOf('{');
            // fixed prefix up to (not including) the first path parameter
            String prefix = braceIdx == -1 ? uri : uri.substring(0, braceIdx);
            grouped.computeIfAbsent(prefix, k -> new ArrayList<>()).add(uri);
        }

        Map<String, Pattern> result = new HashMap<>(grouped.size() * 2);
        grouped.forEach((prefix, templates) -> {
            String combined = templates.stream()
                    // [^/]+ is more correct than \w+ — handles hyphens, dots, UUIDs in path params
                    .map(u -> "(?:" + u.replaceAll("\\{\\w+\\}", "[^/]+") + ")")
                    .collect(Collectors.joining("|"));
            result.put(prefix, Pattern.compile(combined));
        });
        return result;
    }
}
