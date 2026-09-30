package org.egov.requestvalidation.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class InspectionLimitsTest {
    @Test
    void defaultsMatchThePublishedStartingLimits() {
        InspectionLimits limits = InspectionLimits.defaults();

        assertEquals(10 * 1024 * 1024, limits.getMaxBodyBytes());
        assertEquals(64, limits.getMaxDepth());
        assertEquals(1_000_000, limits.getMaxStringLength());
        assertEquals(256, limits.getMaxNameLength());
        assertEquals(2_000_000L, limits.getMaxTokens());
        assertEquals(1000, limits.getMaxNumberLength());
        assertEquals(64 * 1024, limits.getMaxScalarLength());
    }

    @Test
    void rejectsZeroAndUnsafeCeilingsAtConstructionTime() {
        assertThrows(IllegalArgumentException.class,
                () -> new InspectionLimits(0, 1, 1, 1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new InspectionLimits(1, 257, 1, 1, 1, 1, 1));
        assertThrows(IllegalArgumentException.class,
                () -> new InspectionLimits(1, 1, 1, 1,
                        InspectionLimits.MAX_TOKENS_CEILING + 1L, 1, 1));
    }
}
