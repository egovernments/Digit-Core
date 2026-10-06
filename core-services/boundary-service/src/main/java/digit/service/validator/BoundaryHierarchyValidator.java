package digit.service.validator;

import digit.errors.ErrorCodes;
import digit.repository.BoundaryHierarchyRepository;
import digit.web.models.BoundaryTypeHierarchyRequest;
import digit.web.models.BoundaryTypeHierarchySearchCriteria;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class BoundaryHierarchyValidator {

    /**
     * Characters that must not appear in a hierarchyType.
     *
     * Downstream (boundary-management getTransformedLocale, project-factory) every one of these is
     * rewritten to '_' when deriving the localisation module name (hcm-boundary-&lt;type&gt;) and the
     * boundary code prefix. Two hierarchy types differing only by these characters therefore collapse
     * onto the SAME module and the SAME code prefix - e.g. "MODFIX-A", "MODFIX_A" and "MODFIX A" all
     * become "modfix_a" - and silently overwrite each other's data. '_' itself is included because it
     * is the normalization target, so it collides with all the others.
     *
     * The set is written out explicitly rather than using \s: Java's \s is ASCII-only, and
     * Character.isWhitespace() returns false for NBSP (U+00A0), figure space (U+2007) and narrow NBSP
     * (U+202F). This class mirrors JavaScript's \s exactly, so server and client agree character for
     * character.
     */
    private static final Pattern FORBIDDEN_HIERARCHY_TYPE_CHARS = Pattern.compile(
            "[.:\\-/_" +
            "\\u0009\\u000A\\u000B\\u000C\\u000D\\u0020" +   // tab, LF, VT, FF, CR, space
            "\\u00A0\\u1680\\u2000-\\u200A" +                 // NBSP, ogham, en/em/thin/hair spaces
            "\\u2028\\u2029\\u202F\\u205F\\u3000\\uFEFF" +    // line/para sep, narrow NBSP, math, ideographic, BOM
            "]");

    private BoundaryHierarchyRepository boundaryHierarchyRepository;

    @Autowired
    public BoundaryHierarchyValidator(BoundaryHierarchyRepository boundaryHierarchyRepository) {
        this.boundaryHierarchyRepository = boundaryHierarchyRepository;
    }

    /**
     * Parent method for handling boundary hierarchy request validation.
     * @param body
     */
    public void validateBoundaryTypeHierarchy(BoundaryTypeHierarchyRequest body) {

        // Validate that the hierarchy type carries no separator character that would collide
        // downstream. Runs first so a bad name is rejected before any DB lookup.
        validateHierarchyTypeSeparators(body);

        // Validate if only single root node exists
        validateIfSingleRootNodeExists(body);

        // Validate if provided boundary hierarchy forms a directed acyclic graph dependency
        validateIfBoundaryHierarchyFormsDAG(body);

        // Validate if provided boundary hierarchy already exists
        validateIfBoundaryHierarchyAlreadyExists(body);

    }

    /**
     * This method receives boundary type hierarchy request and ensures that the
     * provided hierarchy definition forms a directed acyclic dependency graph.
     * @param body
     */
    private void validateIfBoundaryHierarchyFormsDAG(BoundaryTypeHierarchyRequest body) {

        Map<String, String> parentToChildMap = new LinkedHashMap<>();

        // Populate parent boundaries
        body.getBoundaryHierarchy().getBoundaryHierarchy().forEach(boundaryTypeHierarchy -> {
            parentToChildMap.put(boundaryTypeHierarchy.getBoundaryType(), null);
        });

        // Check if the the hierarchy definition forms a directed acyclic graph
        body.getBoundaryHierarchy().getBoundaryHierarchy().forEach(boundaryTypeHierarchy -> {
            if(!ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType())) {

                if(!parentToChildMap.containsKey(boundaryTypeHierarchy.getParentBoundaryType())) {
                    throw new CustomException(ErrorCodes.INVALID_HIERARCHY_DEFINITION_CODE , ErrorCodes.INVALID_HIERARCHY_DEFINITION_MSG + boundaryTypeHierarchy.getParentBoundaryType());
                }

                if(!ObjectUtils.isEmpty(parentToChildMap.get(boundaryTypeHierarchy.getParentBoundaryType()))) {
                    throw new CustomException(ErrorCodes.INVALID_HIERARCHY_ENTITY_DEFINITION_CODE, ErrorCodes.INVALID_HIERARCHY_ENTITY_DEFINITION_MSG);
                }

                parentToChildMap.put(boundaryTypeHierarchy.getParentBoundaryType(), boundaryTypeHierarchy.getBoundaryType());
            }
        });
    }

    /**
     * Rejects a hierarchyType containing any character that is normalized to '_' downstream, plus '_'
     * itself. Without this, "MODFIX-A" and "MODFIX_A" are accepted as two distinct hierarchies but
     * share one localisation module and one boundary code prefix, so the second silently overwrites
     * the first. Verified live: three such hierarchies produced 30/30 identical boundary codes.
     *
     * Thrown with its own error code so the UI can show a specific message; the offending characters
     * are named in the message (with code points, since several are invisible - NBSP pasted from
     * Word/Excel being the common one).
     * @param body
     */
    private void validateHierarchyTypeSeparators(BoundaryTypeHierarchyRequest body) {

        String hierarchyType = body.getBoundaryHierarchy().getHierarchyType();

        if (ObjectUtils.isEmpty(hierarchyType))
            return;

        // Collect every distinct offending character so the caller can fix them all in one go
        // rather than resubmitting once per bad character.
        Set<String> offenders = new LinkedHashSet<>();
        Matcher matcher = FORBIDDEN_HIERARCHY_TYPE_CHARS.matcher(hierarchyType);
        while (matcher.find()) {
            char offender = matcher.group().charAt(0);
            offenders.add(String.format("'%s' (U+%04X)", describe(offender), (int) offender));
        }

        if (!CollectionUtils.isEmpty(offenders)) {
            throw new CustomException(ErrorCodes.INVALID_HIERARCHY_TYPE_SEPARATOR_CODE,
                    ErrorCodes.INVALID_HIERARCHY_TYPE_SEPARATOR_MSG + String.join(", ", offenders));
        }
    }

    /**
     * Renders a character for an error message. Invisible characters have no useful glyph, so they
     * are named instead - an operator seeing "'-' (U+002D)" can act, but a bare NBSP looks like a
     * space and reads as a service bug.
     */
    private String describe(char c) {
        switch (c) {
            case '\t':     return "tab";
            case '\n':     return "line feed";
            case '\u000B': return "vertical tab";
            case '\f':     return "form feed";
            case '\r':     return "carriage return";
            case '\u0020': return "space";
            case '\u00A0': return "non-breaking space";
            case '\u2007': return "figure space";
            case '\u202F': return "narrow non-breaking space";
            case '\u2028': return "line separator";
            case '\u2029': return "paragraph separator";
            case '\u3000': return "ideographic space";
            case '\uFEFF': return "zero-width no-break space";
            default:
                // Remaining matches are either visible punctuation or one of the Unicode space
                // separators, which have no distinct name worth spelling out individually.
                return Character.isSpaceChar(c) ? "space separator" : String.valueOf(c);
        }
    }

    /**
     * This method validates if only a single root node has been defined in hierarchy definition.
     * @param body
     */
    private void validateIfSingleRootNodeExists(BoundaryTypeHierarchyRequest body) {
        // Get number of nodes whose parent is null
        Long nullParentCount = body.getBoundaryHierarchy().getBoundaryHierarchy().stream()
                .filter(boundaryTypeHierarchy -> ObjectUtils.isEmpty(boundaryTypeHierarchy.getParentBoundaryType()))
                .count();

        if(nullParentCount > 1) {
            throw new CustomException(ErrorCodes.MULTIPLE_ROOT_NODES_ERR_CODE, ErrorCodes.MULTIPLE_ROOT_NODES_ERR_MSG);
        }
    }

    /**
     * This method validates if the provided boundary hierarchy is already created or not.
     * @param body
     */
    private void validateIfBoundaryHierarchyAlreadyExists(BoundaryTypeHierarchyRequest body) {
        // Prepare boundary type hierarchy search criteria
        BoundaryTypeHierarchySearchCriteria boundaryTypeHierarchySearchCriteria = BoundaryTypeHierarchySearchCriteria
                .builder()
                .tenantId(body.getBoundaryHierarchy().getTenantId())
                .hierarchyType(body.getBoundaryHierarchy().getHierarchyType())
                .build();

        // Check if boundary type with the provided tenantId and hierarchy type already exists
        if(!CollectionUtils.isEmpty(boundaryHierarchyRepository.search(boundaryTypeHierarchySearchCriteria))) {
            throw new CustomException(ErrorCodes.DUPLICATE_RECORD_CODE, ErrorCodes.DUPLICATE_RECORD_MSG);
        }
    }

}
