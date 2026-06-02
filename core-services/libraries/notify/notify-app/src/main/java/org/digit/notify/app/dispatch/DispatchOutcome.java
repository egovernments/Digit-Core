package org.digit.notify.app.dispatch;

import org.digit.notify.spi.DispatchResult;
import java.util.List;

public record DispatchOutcome(
    List<DispatchResult> results,
    List<AttemptRecord> attempts
) {}
