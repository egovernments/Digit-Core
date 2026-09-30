package org.egov.requestvalidation.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class SkipPathMatcherTest {
    @Test
    void matchesExactPathAndEveryDescendant() {
        SkipPathMatcher matcher = new SkipPathMatcher(Collections.singletonList("/Mdms/data/help"));

        assertTrue(matcher.matches(Arrays.asList("Mdms", "data", "help")));
        assertTrue(matcher.matches(Arrays.asList("Mdms", "data", "help", "html")));
        assertFalse(matcher.matches(Arrays.asList("Mdms", "data")));
        assertFalse(matcher.matches(Arrays.asList("mdms", "data", "help")));
    }

    @Test
    void wildcardMatchesOneSegmentAndPointerEscapesAreDecoded() {
        SkipPathMatcher matcher = new SkipPathMatcher(Arrays.asList("/items/*/note", "/a~1b/~0key"));

        assertTrue(matcher.matches(Arrays.asList("items", "7", "note")));
        assertFalse(matcher.matches(Arrays.asList("items", "note")));
        assertTrue(matcher.matches(Arrays.asList("a/b", "~key", "child")));
    }

    @Test
    void emptyPointerIsRejectedAndSlashIsTheEmptyNameProperty() {
        assertThrows(IllegalArgumentException.class,
                () -> new SkipPathMatcher(Collections.singletonList("")));

        SkipPathMatcher slash = new SkipPathMatcher(Collections.singletonList("/"));
        assertTrue(slash.matches(Collections.singletonList("")));
        assertTrue(slash.matches(Arrays.asList("", "child")));
        assertFalse(slash.matches(Collections.<String>emptyList()));
        assertFalse(slash.matches(Collections.singletonList("anything")));
    }

    @Test
    void rejectsInvalidPointerEscapesAndPartialWildcards() {
        assertThrows(IllegalArgumentException.class,
                () -> new SkipPathMatcher(Collections.singletonList("missing-slash")));
        assertThrows(IllegalArgumentException.class,
                () -> new SkipPathMatcher(Collections.singletonList("/bad~2escape")));
        assertThrows(IllegalArgumentException.class,
                () -> new SkipPathMatcher(Collections.singletonList("/prefix*")));
    }
}
