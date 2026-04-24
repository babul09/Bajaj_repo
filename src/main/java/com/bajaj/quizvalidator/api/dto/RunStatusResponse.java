package com.bajaj.quizvalidator.api.dto;

import com.bajaj.quizvalidator.domain.scoring.LeaderboardEntry;
import com.bajaj.quizvalidator.domain.scoring.ParticipantTotal;

import java.util.List;

public class RunStatusResponse {

    private final String runId;
    private final String state;
    private final Integer currentPollIndex;
    private final Integer currentAttempt;
    private final Integer retryCount;
    private final String lastError;
    private final String startedAt;
    private final String finishedAt;
    private final Integer uniqueEventCount;
    private final Integer duplicateEventCount;
    private final Integer participantCount;
    private final Integer leaderboardSize;
    private final Integer combinedTotalScore;
    private final String scoringSummary;
    private final List<ParticipantTotal> participantTotals;
    private final List<LeaderboardEntry> leaderboard;
    private final Boolean submissionAttempted;
    private final Boolean duplicateSubmissionBlocked;
    private final Integer totalPollsMade;
    private final Integer submittedTotal;
    private final Integer expectedTotal;
    private final Integer submitAttemptCount;
    private final Boolean isCorrect;
    private final Boolean isIdempotent;
    private final String submitMessage;
    private final String runSummary;

    public RunStatusResponse(
            String runId,
            String state,
            Integer currentPollIndex,
            Integer currentAttempt,
            Integer retryCount,
            String lastError,
            String startedAt,
            String finishedAt,
            Integer uniqueEventCount,
            Integer duplicateEventCount,
            Integer participantCount,
            Integer leaderboardSize,
            Integer combinedTotalScore,
            String scoringSummary,
            List<ParticipantTotal> participantTotals,
            List<LeaderboardEntry> leaderboard,
            Boolean submissionAttempted,
            Boolean duplicateSubmissionBlocked,
            Integer totalPollsMade,
            Integer submittedTotal,
            Integer expectedTotal,
            Integer submitAttemptCount,
            Boolean isCorrect,
            Boolean isIdempotent,
            String submitMessage,
            String runSummary
    ) {
        this.runId = runId;
        this.state = state;
        this.currentPollIndex = currentPollIndex;
        this.currentAttempt = currentAttempt;
        this.retryCount = retryCount;
        this.lastError = lastError;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.uniqueEventCount = uniqueEventCount;
        this.duplicateEventCount = duplicateEventCount;
        this.participantCount = participantCount;
        this.leaderboardSize = leaderboardSize;
        this.combinedTotalScore = combinedTotalScore;
        this.scoringSummary = scoringSummary;
        this.participantTotals = participantTotals == null ? null : List.copyOf(participantTotals);
        this.leaderboard = leaderboard == null ? null : List.copyOf(leaderboard);
        this.submissionAttempted = submissionAttempted;
        this.duplicateSubmissionBlocked = duplicateSubmissionBlocked;
        this.totalPollsMade = totalPollsMade;
        this.submittedTotal = submittedTotal;
        this.expectedTotal = expectedTotal;
        this.submitAttemptCount = submitAttemptCount;
        this.isCorrect = isCorrect;
        this.isIdempotent = isIdempotent;
        this.submitMessage = submitMessage;
        this.runSummary = runSummary;
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

    public Integer getUniqueEventCount() {
        return uniqueEventCount;
    }

    public Integer getDuplicateEventCount() {
        return duplicateEventCount;
    }

    public Integer getParticipantCount() {
        return participantCount;
    }

    public Integer getLeaderboardSize() {
        return leaderboardSize;
    }

    public Integer getCombinedTotalScore() {
        return combinedTotalScore;
    }

    public String getScoringSummary() {
        return scoringSummary;
    }

    public List<ParticipantTotal> getParticipantTotals() {
        return participantTotals;
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return leaderboard;
    }

    public Boolean getSubmissionAttempted() {
        return submissionAttempted;
    }

    public Boolean getDuplicateSubmissionBlocked() {
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

    public Integer getSubmitAttemptCount() {
        return submitAttemptCount;
    }

    public Boolean getIsCorrect() {
        return isCorrect;
    }

    public Boolean getIsIdempotent() {
        return isIdempotent;
    }

    public String getSubmitMessage() {
        return submitMessage;
    }

    public String getRunSummary() {
        return runSummary;
    }
}
