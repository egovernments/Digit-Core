package org.digit.services.boundary;

import java.util.ArrayList;
import java.util.List;
import org.digit.services.boundary.model.BoundarySearchResponse;
import org.digit.services.boundary.model.BoundarySearchResponse.EnrichedBoundary;
import org.digit.services.boundary.model.BoundarySearchResponse.HierarchyRelation;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ancestry resolution, including the malformed trees the service can legitimately produce. */
class BoundaryAncestryTest {

    @Test
    void resolvesRootToLeafPath() {
        BoundarySearchResponse response = tree(
                node("STATE", node("DISTRICT", node("BLOCK", node("VILLAGE")))));

        List<String> path = codes(BoundaryClient.resolveAncestryPath(response, "VILLAGE"));

        assertEquals(List.of("STATE", "DISTRICT", "BLOCK", "VILLAGE"), path);
    }

    @Test
    void returnsJustTheRootWhenTheRootIsTheTarget() {
        BoundarySearchResponse response = tree(node("STATE", node("DISTRICT")));
        assertEquals(List.of("STATE"), codes(BoundaryClient.resolveAncestryPath(response, "STATE")));
    }

    @Test
    void returnsEmptyWhenBoundaryListIsNull() {
        // The service sends "boundary": null rather than [] when nothing matched.
        HierarchyRelation relation = new HierarchyRelation();
        relation.setHierarchyType("REVENUE");
        relation.setBoundary(null);
        BoundarySearchResponse response = new BoundarySearchResponse(List.of(relation));

        assertTrue(BoundaryClient.resolveAncestryPath(response, "VILLAGE").isEmpty());
    }

    @Test
    void returnsEmptyWhenTargetIsAbsent() {
        BoundarySearchResponse response = tree(node("STATE", node("DISTRICT")));
        assertTrue(BoundaryClient.resolveAncestryPath(response, "NOWHERE").isEmpty());
    }

    @Test
    void returnsEmptyForNullInputs() {
        assertTrue(BoundaryClient.resolveAncestryPath(null, "VILLAGE").isEmpty());
        assertTrue(BoundaryClient.resolveAncestryPath(tree(node("STATE")), null).isEmpty());
    }

    @Test
    void picksTheCorrectBranch() {
        BoundarySearchResponse response = tree(
                node("STATE",
                        node("DISTRICT_A", node("BLOCK_A")),
                        node("DISTRICT_B", node("BLOCK_B"))));

        assertEquals(List.of("STATE", "DISTRICT_B", "BLOCK_B"),
                codes(BoundaryClient.resolveAncestryPath(response, "BLOCK_B")));
    }

    @Test
    void searchesEveryRootWhenThereAreSeveral() {
        BoundarySearchResponse response = tree(node("STATE_A", node("D_A")), node("STATE_B", node("D_B")));
        assertEquals(List.of("STATE_B", "D_B"), codes(BoundaryClient.resolveAncestryPath(response, "D_B")));
    }

    @Test
    void survivesACycle() {
        // Corrupt parent data: a node that lists an ancestor as its own child.
        EnrichedBoundary state = node("STATE");
        EnrichedBoundary district = node("DISTRICT");
        state.setChildren(List.of(district));
        district.setChildren(List.of(state));

        // Terminates rather than recursing forever, and still finds a reachable target.
        assertEquals(List.of("STATE", "DISTRICT"),
                codes(BoundaryClient.resolveAncestryPath(tree(state), "DISTRICT")));
        assertTrue(BoundaryClient.resolveAncestryPath(tree(state), "MISSING").isEmpty());
    }

    @Test
    void stopsAtTheDepthCap() {
        // 200 levels deep: beyond the cap, so the target below it is not reported.
        EnrichedBoundary deepest = node("LEAF");
        EnrichedBoundary current = deepest;
        for (int i = 0; i < 200; i++) {
            EnrichedBoundary parent = node("LEVEL_" + i);
            parent.setChildren(List.of(current));
            current = parent;
        }
        assertTrue(BoundaryClient.resolveAncestryPath(tree(current), "LEAF").isEmpty());
    }

    @Test
    void skipsNodesWithNoCode() {
        EnrichedBoundary root = node("STATE");
        EnrichedBoundary broken = new EnrichedBoundary();
        root.setChildren(List.of(broken, node("DISTRICT")));

        assertEquals(List.of("STATE", "DISTRICT"),
                codes(BoundaryClient.resolveAncestryPath(tree(root), "DISTRICT")));
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static EnrichedBoundary node(String code, EnrichedBoundary... children) {
        EnrichedBoundary boundary = new EnrichedBoundary();
        boundary.setCode(code);
        boundary.setBoundaryType(code);
        boundary.setChildren(children.length == 0 ? null : new ArrayList<>(List.of(children)));
        return boundary;
    }

    private static BoundarySearchResponse tree(EnrichedBoundary... roots) {
        HierarchyRelation relation = new HierarchyRelation();
        relation.setTenantId("TEST3");
        relation.setHierarchyType("REVENUE");
        relation.setBoundary(List.of(roots));
        return new BoundarySearchResponse(List.of(relation));
    }

    private static List<String> codes(List<EnrichedBoundary> path) {
        return path.stream().map(EnrichedBoundary::getCode).toList();
    }
}
