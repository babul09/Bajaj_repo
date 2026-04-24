package com.bajaj.quizvalidator.domain.scoring;

import com.bajaj.quizvalidator.integration.dto.PollResponse;

import java.util.Objects;

public record ScoringEvent(String roundId, String participant, int score) {

    public ScoringEvent {
        if (roundId == null || roundId.isBlank()) {
            throw new IllegalArgumentException("roundId must be provided");
        }
        if (participant == null || participant.isBlank()) {
            throw new IllegalArgumentException("participant must be provided");
        }
    }

    public static ScoringEvent from(PollResponse.PollEvent pollEvent) {
        Objects.requireNonNull(pollEvent, "pollEvent must be provided");
        if (pollEvent.getScore() == null) {
            throw new IllegalArgumentException("score must be provided");
        }
        return new ScoringEvent(pollEvent.getRoundId(), pollEvent.getParticipant(), pollEvent.getScore());
    }
}