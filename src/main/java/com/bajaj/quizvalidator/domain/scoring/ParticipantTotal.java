package com.bajaj.quizvalidator.domain.scoring;

public record ParticipantTotal(String participant, int totalScore) {

    public ParticipantTotal {
        if (participant == null || participant.isBlank()) {
            throw new IllegalArgumentException("participant must be provided");
        }
    }
}