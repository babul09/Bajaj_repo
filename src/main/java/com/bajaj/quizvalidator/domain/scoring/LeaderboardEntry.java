package com.bajaj.quizvalidator.domain.scoring;

public record LeaderboardEntry(String participant, int totalScore) {

    public LeaderboardEntry {
        if (participant == null || participant.isBlank()) {
            throw new IllegalArgumentException("participant must be provided");
        }
    }
}