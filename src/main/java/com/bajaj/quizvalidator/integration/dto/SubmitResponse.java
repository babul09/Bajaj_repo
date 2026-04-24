package com.bajaj.quizvalidator.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SubmitResponse {

    private String regNo;
    private Integer totalPollsMade;
    private Integer submittedTotal;
    private Integer expectedTotal;
    private Integer attemptCount;
    private Boolean isCorrect;
    private Boolean isIdempotent;
    private String message;

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public Integer getTotalPollsMade() {
        return totalPollsMade;
    }

    public void setTotalPollsMade(Integer totalPollsMade) {
        this.totalPollsMade = totalPollsMade;
    }

    public Integer getSubmittedTotal() {
        return submittedTotal;
    }

    public void setSubmittedTotal(Integer submittedTotal) {
        this.submittedTotal = submittedTotal;
    }

    public Integer getExpectedTotal() {
        return expectedTotal;
    }

    public void setExpectedTotal(Integer expectedTotal) {
        this.expectedTotal = expectedTotal;
    }

    public Integer getAttemptCount() {
        return attemptCount;
    }

    public void setAttemptCount(Integer attemptCount) {
        this.attemptCount = attemptCount;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public void setIsCorrect(Boolean correct) {
        isCorrect = correct;
    }

    public Boolean getIsIdempotent() {
        return isIdempotent;
    }

    public void setIsIdempotent(Boolean idempotent) {
        isIdempotent = idempotent;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
