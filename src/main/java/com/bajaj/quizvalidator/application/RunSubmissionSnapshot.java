package com.bajaj.quizvalidator.application;

import com.bajaj.quizvalidator.integration.dto.SubmitResponse;

public class RunSubmissionSnapshot {

    private final boolean submissionAttempted;
    private final boolean duplicateSubmissionBlocked;
    private final Integer totalPollsMade;
    private final Integer submittedTotal;
    private final Integer expectedTotal;
    private final Integer attemptCount;
    private final Boolean correct;
    private final Boolean idempotent;
    private final String message;

    private RunSubmissionSnapshot(
            boolean submissionAttempted,
            boolean duplicateSubmissionBlocked,
            Integer totalPollsMade,
            Integer submittedTotal,
            Integer expectedTotal,
            Integer attemptCount,
            Boolean correct,
            Boolean idempotent,
            String message
    ) {
        this.submissionAttempted = submissionAttempted;
        this.duplicateSubmissionBlocked = duplicateSubmissionBlocked;
        this.totalPollsMade = totalPollsMade;
        this.submittedTotal = submittedTotal;
        this.expectedTotal = expectedTotal;
        this.attemptCount = attemptCount;
        this.correct = correct;
        this.idempotent = idempotent;
        this.message = message;
    }

    public static RunSubmissionSnapshot from(SubmitResponse submitResponse, boolean duplicateSubmissionBlocked) {
        return new RunSubmissionSnapshot(
                true,
                duplicateSubmissionBlocked,
                submitResponse == null ? null : submitResponse.getTotalPollsMade(),
                submitResponse == null ? null : submitResponse.getSubmittedTotal(),
                submitResponse == null ? null : submitResponse.getExpectedTotal(),
                submitResponse == null ? null : submitResponse.getAttemptCount(),
                submitResponse == null ? null : submitResponse.getIsCorrect(),
                submitResponse == null ? null : submitResponse.getIsIdempotent(),
                submitResponse == null ? null : submitResponse.getMessage()
        );
    }

    public boolean isSubmissionAttempted() {
        return submissionAttempted;
    }

    public boolean isDuplicateSubmissionBlocked() {
        return duplicateSubmissionBlocked;
    }

    public Integer getTotalPollsMade() {
        return totalPollsMade;
    }

    public Integer getSubmittedTotal() {
        return submittedTotal;
    }

    public Integer getExpectedTotal() {
        return expectedTotal;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public Boolean getCorrect() {
        return correct;
    }

    public Boolean getIdempotent() {
        return idempotent;
    }

    public String getMessage() {
        return message;
    }

    public String getReviewSummary() {
        return "submissionAttempted=" + submissionAttempted
                + ", duplicateSubmissionBlocked=" + duplicateSubmissionBlocked
                + ", totalPollsMade=" + totalPollsMade
                + ", submittedTotal=" + submittedTotal
                + ", expectedTotal=" + expectedTotal
                + ", attemptCount=" + attemptCount
                + ", isCorrect=" + correct
                + ", isIdempotent=" + idempotent
                + ", message=" + message;
    }
}
