package digit.service.validator;

import digit.errors.ErrorCodes;
import digit.repository.BoundaryHierarchyRepository;
import digit.web.models.BoundaryTypeHierarchy;
import digit.web.models.BoundaryTypeHierarchyDefinition;
import digit.web.models.BoundaryTypeHierarchyRequest;
import org.egov.tracer.model.CustomException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the hierarchyType separator guard: every character that getTransformedLocale rewrites to
 * '_' (plus '_' itself) must be rejected, and nothing else may be.
 */
class BoundaryHierarchyTypeSeparatorTest {

    private BoundaryHierarchyValidator validator() {
        BoundaryHierarchyRepository repo = mock(BoundaryHierarchyRepository.class);
        // Not reached for the rejection cases; empty so accepted names fall through cleanly.
        when(repo.search(org.mockito.ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        return new BoundaryHierarchyValidator(repo);
    }

    private BoundaryTypeHierarchyRequest requestFor(String hierarchyType) {
        BoundaryTypeHierarchy node = BoundaryTypeHierarchy.builder()
                .boundaryType("COUNTRY")
                .parentBoundaryType(null)
                .active(Boolean.TRUE)
                .build();
        BoundaryTypeHierarchyDefinition definition = BoundaryTypeHierarchyDefinition.builder()
                .tenantId("dev")
                .hierarchyType(hierarchyType)
                .boundaryHierarchy(List.of(node))
                .build();
        return BoundaryTypeHierarchyRequest.builder()
                .boundaryHierarchy(definition)
                .build();
    }

    private static final char[] MUST_REJECT = {
            '.', ':', '-', '/', '_', '\t',
            '\n', '\u000B', '\f', '\r', '\u0020', '\u00A0',
            '\u1680', '\u2000', '\u2001', '\u2002', '\u2003', '\u2004',
            '\u2005', '\u2006', '\u2007', '\u2008', '\u2009', '\u200A',
            '\u2028', '\u2029', '\u202F', '\u205F', '\u3000', '\uFEFF'
    };

    @Test
    void rejectsEveryCharacterThatNormalizesToUnderscore() {
        BoundaryHierarchyValidator v = validator();
        assertEquals(30, MUST_REJECT.length, "expected 29 transform chars + '_' itself");

        for (char c : MUST_REJECT) {
            String hierarchyType = "MODFIX" + c + "A";
            CustomException ex = assertThrows(CustomException.class,
                    () -> v.validateBoundaryTypeHierarchy(requestFor(hierarchyType)),
                    () -> String.format("U+%04X should have been rejected", (int) c));
            assertEquals(ErrorCodes.INVALID_HIERARCHY_TYPE_SEPARATOR_CODE, ex.getCode(),
                    () -> String.format("U+%04X rejected with the wrong error code", (int) c));
            assertTrue(ex.getMessage().contains(String.format("U+%04X", (int) c)),
                    () -> String.format("message should name the offending code point U+%04X", (int) c));
        }
    }

    @Test
    void acceptsCleanHierarchyTypes() {
        BoundaryHierarchyValidator v = validator();
        for (String ok : List.of("MODFIXA", "LNPERF115K", "MICROPLAN", "SIERRALEON", "Nigeria2026")) {
            assertDoesNotThrow(() -> v.validateBoundaryTypeHierarchy(requestFor(ok)),
                    () -> ok + " should be accepted");
        }
    }

    @Test
    void doesNotRejectSemicolonOrOtherPunctuation() {
        // ';' looks like it falls in a ':'-to-'s' range but the '-' in the source regex is literal.
        // Guards against someone "simplifying" the class into an actual range.
        BoundaryHierarchyValidator v = validator();
        for (String ok : List.of("MODFIX;A", "MODFIX@A", "MODFIX+A", "MODFIX=A", "MODFIX(A)")) {
            assertDoesNotThrow(() -> v.validateBoundaryTypeHierarchy(requestFor(ok)),
                    () -> ok + " must not be rejected by the separator guard");
        }
    }

    @Test
    void reportsAllDistinctOffendersAtOnce() {
        BoundaryHierarchyValidator v = validator();
        CustomException ex = assertThrows(CustomException.class,
                () -> v.validateBoundaryTypeHierarchy(requestFor("A-B_C D.E")));
        assertTrue(ex.getMessage().contains("U+002D"), "should name hyphen");
        assertTrue(ex.getMessage().contains("U+005F"), "should name underscore");
        assertTrue(ex.getMessage().contains("U+0020"), "should name space");
        assertTrue(ex.getMessage().contains("U+002E"), "should name period");
    }

    @Test
    void namesInvisibleCharactersReadably() {
        BoundaryHierarchyValidator v = validator();
        CustomException nbsp = assertThrows(CustomException.class,
                () -> v.validateBoundaryTypeHierarchy(requestFor("CHAD" + "\u00A0" + "SMC")));
        assertTrue(nbsp.getMessage().contains("non-breaking space"),
                "NBSP must be named, not rendered as an invisible glyph: " + nbsp.getMessage());
        assertTrue(nbsp.getMessage().contains("U+00A0"));
    }

    @Test
    void nullHierarchyTypeIsLeftToOtherValidators() {
        BoundaryHierarchyValidator v = validator();
        // Null/blank naming is not this validator's concern; it must not throw the separator error.
        try {
            v.validateBoundaryTypeHierarchy(requestFor(null));
        } catch (CustomException ex) {
            assertNotEquals(ErrorCodes.INVALID_HIERARCHY_TYPE_SEPARATOR_CODE, ex.getCode());
        }
    }
}
