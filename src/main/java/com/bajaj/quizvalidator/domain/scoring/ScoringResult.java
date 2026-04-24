package com.bajaj.quizvalidator.domain.scoring;

import java.util.List;

public record ScoringResult(
        List<ParticipantTotal> participantTotals,
        List<LeaderboardEntry> leaderboard,
        int uniqueEventCount,
        int duplicateEventCount,
        int combinedTotalScore
) {

    public ScoringResult {
        participantTotals = List.copyOf(participantTotals);
        leaderboard = List.copyOf(leaderboard);
    }
}