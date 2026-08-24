package org.digit.util;

import java.util.concurrent.Callable;

/**
 * Holds the {@link DigitRequestContext} for the current thread.
 *
 * <p>Set this when there is no inbound servlet request to propagate headers from. The header
 * interceptor prefers an explicitly set context over the ambient request, so existing
 * request-scoped behaviour is unchanged whenever nothing is set here.
 *
 * <p>Prefer the scoped {@link #run(DigitRequestContext, Runnable)} /
 * {@link #call(DigitRequestContext, Callable)} helpers over bare {@code set}: they restore whatever
 * was previously in place, so a nested call cannot leak its context to the caller, and a pooled
 * thread cannot carry one into unrelated work.
 */
public final class DigitContextHolder {

    private static final ThreadLocal<DigitRequestContext> CONTEXT = new ThreadLocal<>();

    private DigitContextHolder() {
    }

    public static DigitRequestContext get() {
        return CONTEXT.get();
    }

    public static void set(DigitRequestContext context) {
        if (context == null) {
            CONTEXT.remove();
        } else {
            CONTEXT.set(context);
        }
    }

    public static void clear() {
        CONTEXT.remove();
    }

    /** Runs {@code action} with {@code context} in force, restoring the previous context after. */
    public static void run(DigitRequestContext context, Runnable action) {
        DigitRequestContext previous = CONTEXT.get();
        set(context);
        try {
            action.run();
        } finally {
            set(previous);
        }
    }

    /** As {@link #run}, for work that returns a value or throws a checked exception. */
    public static <T> T call(DigitRequestContext context, Callable<T> action) throws Exception {
        DigitRequestContext previous = CONTEXT.get();
        set(context);
        try {
            return action.call();
        } finally {
            set(previous);
        }
    }
}
