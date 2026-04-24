package com.bajaj.quizvalidator.application;

import com.bajaj.quizvalidator.domain.scoring.LeaderboardEntry;
import com.bajaj.quizvalidator.domain.scoring.ParticipantTotal;
import com.bajaj.quizvalidator.domain.scoring.ScoringResult;

import java.util.List;

public class RunScoringSnapshot {

    private final int uniqueEventCount;
    private final int duplicateEventCount;
    private final int participantCount;
    private final int leaderboardSize;
    private final List<ParticipantTotal> participantTotals;
    private final List<LeaderboardEntry> leaderboard;
    private final int combinedTotalScore;

    private RunScoringSnapshot(
            int uniqueEventCount,
            int duplicateEventCount,
            int participantCount,
            int leaderboardSize,
            List<ParticipantTotal> participantTotals,
            List<LeaderboardEntry> leaderboard,
            int combinedTotalScore
    ) {
        this.uniqueEventCount = uniqueEventCount;
        this.duplicateEventCount = duplicateEventCount;
        this.participantCount = participantCount;
        this.leaderboardSize = leaderboardSize;
        this.participantTotals = List.copyOf(participantTotals);
        this.leaderboard = List.copyOf(leaderboard);
        this.combinedTotalScore = combinedTotalScore;
    }

    public static RunScoringSnapshot from(ScoringResult scoringResult) {
        return new RunScoringSnapshot(
                scoringResult.uniqueEventCount(),
                scoringResult.duplicateEventCount(),
                scoringResult.participantTotals().size(),
                scoringResult.leaderboard().size(),
                scoringResult.participantTotals(),
                scoringResult.leaderboard(),
                scoringResult.combinedTotalScore()
        );
    }

    public int getUniqueEventCount() {
        return uniqueEventCount;
    }

    public int getDuplicateEventCount() {
        return duplicateEventCount;
    }

    public int getParticipantCount() {
        return participantCount;
    }

    public List<ParticipantTotal> getParticipantTotals() {
        return participantTotals;
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return leaderboard;
    }

    public int getLeaderboardSize() {
        return leaderboardSize;
    }

    public int getCombinedTotalScore() {
        return combinedTotalScore;
    }

    public String getReviewSummary() {
        return "uniqueEvents=" + uniqueEventCount
                + ", duplicates=" + duplicateEventCount
                + ", participants=" + participantCount
                + ", leaderboardSize=" + leaderboardSize
                + ", combinedTotal=" + combinedTotalScore
                + ", leaderboard=" + formatLeaderboard();
    }

    private String formatLeaderboard() {
        return leaderboard.stream()
                .map(entry -> entry.participant() + "=" + entry.totalScore())
                .toList()
                .toString();
    }
}
