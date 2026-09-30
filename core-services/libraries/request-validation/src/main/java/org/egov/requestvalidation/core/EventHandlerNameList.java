package org.egov.requestvalidation.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

final class EventHandlerNameList {
    private static final String RESOURCE = "/org/egov/requestvalidation/event-handler-names.txt";

    private EventHandlerNameList() {
    }

    static Set<String> load() {
        InputStream stream = EventHandlerNameList.class.getResourceAsStream(RESOURCE);
        if (stream == null) {
            throw new IllegalStateException("Event-handler name resource is missing");
        }
        LinkedHashSet<String> names = new LinkedHashSet<String>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String value = line.trim().toLowerCase(Locale.ROOT);
                if (!value.isEmpty() && !value.startsWith("#")) {
                    if (!value.matches("on[a-z0-9]+")) {
                        throw new IllegalStateException("Event-handler resource contains an invalid name");
                    }
                    names.add(value);
                }
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Event-handler name resource cannot be read");
        }
        if (names.isEmpty()) {
            throw new IllegalStateException("Event-handler name resource is empty");
        }
        return Collections.unmodifiableSet(names);
    }
}
