package com.bajaj.quizvalidator.application;

import com.bajaj.quizvalidator.integration.dto.SubmitResponse;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunSubmissionGuardTest {

    @Test
    void blocksDuplicateSubmissionAttemptWithoutInvokingSupplierTwice() {
        RunSubmissionGuard guard = new RunSubmissionGuard();
        AtomicInteger supplierCalls = new AtomicInteger();

        RunSubmissionGuard.SubmissionAttempt firstAttempt = guard.submitOnce(() -> {
            supplierCalls.incrementAndGet();
            SubmitResponse submitResponse = new SubmitResponse();
            submitResponse.setSubmittedTotal(22);
            return submitResponse;
        });

        RunSubmissionGuard.SubmissionAttempt duplicateAttempt = guard.submitOnce(() -> {
            supplierCalls.incrementAndGet();
            throw new IllegalStateException("duplicate supplier should not run");
        });

        assertTrue(firstAttempt.executed());
        assertFalse(firstAttempt.duplicateBlocked());
        assertEquals(22, firstAttempt.submitResponse().getSubmittedTotal());
        assertFalse(duplicateAttempt.executed());
        assertTrue(duplicateAttempt.duplicateBlocked());
        assertEquals(1, supplierCalls.get());
    }
}
