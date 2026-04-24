package com.bajaj.quizvalidator.integration.dto;

public class PollRequest {

    private final String regNo;
    private final int poll;

    public PollRequest(String regNo, int poll) {
        this.regNo = regNo;
        this.poll = poll;
    }

    public String getRegNo() {
        return regNo;
    }

    public int getPoll() {
        return poll;
    }
}
