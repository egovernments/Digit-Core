package org.egov.requestvalidation.core;

/** Validated resource limits used by request inspection. */
public final class InspectionLimits {
    public static final int MAX_BODY_BYTES_CEILING = Integer.MAX_VALUE - 1;
    public static final int MAX_DEPTH_CEILING = 256;
    public static final int MAX_TEXT_LENGTH_CEILING = Integer.MAX_VALUE - 1;
    public static final long MAX_TOKENS_CEILING = Integer.MAX_VALUE;

    private final int maxBodyBytes;
    private final int maxDepth;
    private final int maxStringLength;
    private final int maxNameLength;
    private final long maxTokens;
    private final int maxNumberLength;
    private final int maxScalarLength;

    public InspectionLimits(int maxBodyBytes, int maxDepth, int maxStringLength,
            int maxNameLength, long maxTokens, int maxNumberLength, int maxScalarLength) {
        this.maxBodyBytes = positiveAtMost(maxBodyBytes, MAX_BODY_BYTES_CEILING, "maxBodyBytes");
        this.maxDepth = positiveAtMost(maxDepth, MAX_DEPTH_CEILING, "maxDepth");
        this.maxStringLength = positiveAtMost(
                maxStringLength, MAX_TEXT_LENGTH_CEILING, "maxStringLength");
        this.maxNameLength = positiveAtMost(
                maxNameLength, MAX_TEXT_LENGTH_CEILING, "maxNameLength");
        this.maxTokens = positiveAtMost(maxTokens, MAX_TOKENS_CEILING, "maxTokens");
        this.maxNumberLength = positiveAtMost(
                maxNumberLength, MAX_TEXT_LENGTH_CEILING, "maxNumberLength");
        this.maxScalarLength = positiveAtMost(
                maxScalarLength, MAX_TEXT_LENGTH_CEILING, "maxScalarLength");
    }

    public static InspectionLimits defaults() {
        return new InspectionLimits(10 * 1024 * 1024, 64, 1_000_000,
                256, 2_000_000L, 1000, 64 * 1024);
    }

    public int getMaxBodyBytes() {
        return maxBodyBytes;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public int getMaxStringLength() {
        return maxStringLength;
    }

    public int getMaxNameLength() {
        return maxNameLength;
    }

    public long getMaxTokens() {
        return maxTokens;
    }

    public int getMaxNumberLength() {
        return maxNumberLength;
    }

    public int getMaxScalarLength() {
        return maxScalarLength;
    }

    private static int positiveAtMost(int value, int ceiling, String name) {
        if (value <= 0 || value > ceiling) {
            throw new IllegalArgumentException(name + " must be between 1 and " + ceiling);
        }
        return value;
    }

    private static long positiveAtMost(long value, long ceiling, String name) {
        if (value <= 0L || value > ceiling) {
            throw new IllegalArgumentException(name + " must be between 1 and " + ceiling);
        }
        return value;
    }
}
