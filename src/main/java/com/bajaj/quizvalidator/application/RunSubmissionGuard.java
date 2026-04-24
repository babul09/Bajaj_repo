package com.bajaj.quizvalidator.application;

import com.bajaj.quizvalidator.integration.dto.SubmitResponse;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

class RunSubmissionGuard {

    private final AtomicBoolean submitted = new AtomicBoolean(false);

    SubmissionAttempt submitOnce(Supplier<SubmitResponse> submissionSupplier) {
        if (!submitted.compareAndSet(false, true)) {
            return SubmissionAttempt.blocked();
        }
        return SubmissionAttempt.executed(submissionSupplier.get());
    }

    record SubmissionAttempt(boolean executed, boolean duplicateBlocked, SubmitResponse submitResponse) {

        static SubmissionAttempt executed(SubmitResponse submitResponse) {
            return new SubmissionAttempt(true, false, submitResponse);
        }

        static SubmissionAttempt blocked() {
            return new SubmissionAttempt(false, true, null);
        }
    }
}
