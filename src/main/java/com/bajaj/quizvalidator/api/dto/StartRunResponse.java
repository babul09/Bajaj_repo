package com.bajaj.quizvalidator.api.dto;

public class StartRunResponse {

    private final String runId;
    private final String message;

    public StartRunResponse(String runId, String message) {
        this.runId = runId;
        this.message = message;
    }

    public String getRunId() {
        return runId;
    }

    public String getMessage() {
        return message;
    }
}
