package com.bajaj.quizvalidator.api.dto;

public class RunStatusResponse {

    private final String runId;
    private final String state;
    private final Integer currentPollIndex;
    private final Integer currentAttempt;
    private final Integer retryCount;
    private final String lastError;
    private final String startedAt;
    private final String finishedAt;

    public RunStatusResponse(
            String runId,
            String state,
            Integer currentPollIndex,
            Integer currentAttempt,
            Integer retryCount,
            String lastError,
            String startedAt,
            String finishedAt
    ) {
        this.runId = runId;
        this.state = state;
        this.currentPollIndex = currentPollIndex;
        this.currentAttempt = currentAttempt;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
    }

    public String getRunId() {
        return runId;
    }

    public String getState() {
        return state;
    }

    public Integer getCurrentPollIndex() {
        return currentPollIndex;
    }

    public Integer getCurrentAttempt() {
        return currentAttempt;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public String getLastError() {
        return lastError;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public String getFinishedAt() {
        return finishedAt;
    }
}
