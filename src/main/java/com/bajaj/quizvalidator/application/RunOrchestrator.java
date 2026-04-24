package com.bajaj.quizvalidator.application;

import com.bajaj.quizvalidator.api.dto.RunStatusResponse;
import com.bajaj.quizvalidator.config.ValidatorProperties;
import com.bajaj.quizvalidator.domain.scoring.ScoringEngine;
import com.bajaj.quizvalidator.domain.scoring.ScoringResult;
import com.bajaj.quizvalidator.integration.ValidatorClient;
import com.bajaj.quizvalidator.integration.dto.PollResponse;
import com.bajaj.quizvalidator.integration.dto.SubmitResponse;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class RunOrchestrator {

    private static final Logger LOGGER = LoggerFactory.getLogger(RunOrchestrator.class);

    private final ValidatorClient validatorClient;
    private final ValidatorProperties validatorProperties;
    private final ScoringEngine scoringEngine;
    private final Executor runExecutor;

    private PollDelayEnforcer pollDelayEnforcer;
    private RetryPolicy retryPolicy;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private volatile RunStatus status = RunStatus.idle();

    public RunOrchestrator(
            ValidatorClient validatorClient,
            ValidatorProperties validatorProperties,
            ScoringEngine scoringEngine,
            @Qualifier("runExecutor") Executor runExecutor
    ) {
        this.validatorClient = validatorClient;
        this.validatorProperties = validatorProperties;
        this.scoringEngine = scoringEngine;
        this.runExecutor = runExecutor;
    }

    @PostConstruct
    void initializePolicies() {
        this.pollDelayEnforcer = new PollDelayEnforcer(Duration.ofMillis(validatorProperties.getMinPollIntervalMs()));
        this.retryPolicy = new RetryPolicy(validatorProperties.getRetryMaxAttempts());
    }

    public StartResult startRun(String regNo) {
        if (regNo == null || regNo.isBlank()) {
            throw new IllegalArgumentException("regNo must be provided");
        }
        if (!running.compareAndSet(false, true)) {
            return StartResult.rejected("Another run is already in progress");
        }

        String runId = UUID.randomUUID().toString();
        status = RunStatus.running(runId);

        runExecutor.execute(() -> executeRun(runId, regNo));
        return StartResult.accepted(runId);
    }

    public void executeRunBlockingForTest(String runId, String regNo) {
        if (!running.compareAndSet(false, true)) {
            throw new IllegalStateException("Another run is already in progress");
        }
        status = RunStatus.running(runId);
        executeRun(runId, regNo);
    }

    public RunStatusResponse currentStatus() {
        RunStatus current = status;
        return new RunStatusResponse(
                current.runId,
                current.state,
                current.currentPollIndex,
                current.currentAttempt,
                current.retryCount,
                current.lastError,
                formatInstant(current.startedAt),
                formatInstant(current.finishedAt),
                current.scoringSnapshot == null ? null : current.scoringSnapshot.getUniqueEventCount(),
                current.scoringSnapshot == null ? null : current.scoringSnapshot.getDuplicateEventCount(),
                current.scoringSnapshot == null ? null : current.scoringSnapshot.getParticipantCount(),
                current.scoringSnapshot == null ? null : current.scoringSnapshot.getLeaderboardSize(),
                current.scoringSnapshot == null ? null : current.scoringSnapshot.getCombinedTotalScore(),
                current.scoringSnapshot == null ? null : current.scoringSnapshot.getReviewSummary(),
                current.scoringSnapshot == null ? null : current.scoringSnapshot.getParticipantTotals(),
                current.scoringSnapshot == null ? null : current.scoringSnapshot.getLeaderboard(),
                current.submissionSnapshot == null ? null : current.submissionSnapshot.isSubmissionAttempted(),
                current.submissionSnapshot == null ? null : current.submissionSnapshot.isDuplicateSubmissionBlocked(),
                current.submissionSnapshot == null ? null : current.submissionSnapshot.getTotalPollsMade(),
                current.submissionSnapshot == null ? null : current.submissionSnapshot.getSubmittedTotal(),
                current.submissionSnapshot == null ? null : current.submissionSnapshot.getExpectedTotal(),
                current.submissionSnapshot == null ? null : current.submissionSnapshot.getAttemptCount(),
                current.submissionSnapshot == null ? null : current.submissionSnapshot.getCorrect(),
                current.submissionSnapshot == null ? null : current.submissionSnapshot.getIdempotent(),
                current.submissionSnapshot == null ? null : current.submissionSnapshot.getMessage(),
                buildRunSummary(current.scoringSnapshot, current.submissionSnapshot)
        );
    }

    private void executeRun(String runId, String regNo) {
        Instant previousRequestStart = null;
        List<PollResponse> collectedPollResponses = new ArrayList<>();
        try {
            for (int pollIndex = 0; pollIndex <= 9; pollIndex++) {
                boolean completedPoll = false;
                int attempt = 1;

                while (!completedPoll) {
                    pollDelayEnforcer.enforceFrom(previousRequestStart);
                    Instant requestStart = Instant.now();
                    previousRequestStart = requestStart;

                    status = status.withProgress(pollIndex, attempt);
                    LOGGER.info("poll_index={} attempt={} outcome=started", pollIndex, attempt);

                    try {
                        PollResponse pollResponse = validatorClient.fetchMessages(regNo, pollIndex);
                        if (pollResponse == null) {
                            throw new IllegalStateException("Validator returned no poll response for poll index " + pollIndex);
                        }
                        collectedPollResponses.add(pollResponse);
                        LOGGER.info("poll_index={} attempt={} outcome=success", pollIndex, attempt);
                        completedPoll = true;
                    } catch (Exception exception) {
                        boolean retryable = retryPolicy.isRetryable(exception);
                        boolean hasAttemptsRemaining = attempt < retryPolicy.getMaxAttempts();

                        LOGGER.warn(
                                "poll_index={} attempt={} outcome=failure retryable={} reason={}",
                                pollIndex,
                                attempt,
                                retryable,
                                exception.getMessage()
                        );

                        if (retryable && hasAttemptsRemaining) {
                            status = status.incrementRetryCount();
                            LOGGER.info("poll_index={} attempt={} outcome=retrying", pollIndex, attempt);
                            attempt++;
                            continue;
                        }

                        throw new IllegalStateException(
                                "Poll failed at index " + pollIndex + " after attempt " + attempt + ": " + exception.getMessage(),
                                exception
                        );
                    }
                }
            }

            ScoringResult scoringResult = scoringEngine.score(collectedPollResponses);
            RunScoringSnapshot scoringSnapshot = RunScoringSnapshot.from(scoringResult);
            RunSubmissionSnapshot submissionSnapshot = submitLeaderboard(runId, regNo, scoringSnapshot);
            LOGGER.info(
                    "run_id={} scoring_summary={}",
                    runId,
                    scoringSnapshot.getReviewSummary()
            );
            LOGGER.info(
                    "run_id={} submit_summary={}",
                    runId,
                    submissionSnapshot.getReviewSummary()
            );
            status = status.complete(scoringSnapshot, submissionSnapshot);
            LOGGER.info("run_id={} outcome=completed", runId);
        } catch (Exception exception) {
            status = status.fail(exception.getMessage());
            LOGGER.error("run_id={} outcome=failed reason={}", runId, exception.getMessage(), exception);
        } finally {
            running.set(false);
        }
    }

    private String formatInstant(Instant instant) {
        return instant == null ? null : DateTimeFormatter.ISO_INSTANT.format(instant);
    }

    private RunSubmissionSnapshot submitLeaderboard(String runId, String regNo, RunScoringSnapshot scoringSnapshot) {
        RunSubmissionGuard submissionGuard = new RunSubmissionGuard();
        RunSubmissionGuard.SubmissionAttempt submissionAttempt = submissionGuard.submitOnce(
                () -> validatorClient.submitLeaderboard(regNo, scoringSnapshot.getLeaderboard())
        );

        if (!submissionAttempt.executed()) {
            throw new IllegalStateException("Submission was blocked before the first validator call for run " + runId);
        }

        RunSubmissionGuard.SubmissionAttempt duplicateAttempt = submissionGuard.submitOnce(() -> {
            throw new IllegalStateException("Duplicate submission supplier should not be invoked");
        });

        if (duplicateAttempt.duplicateBlocked()) {
            LOGGER.info("run_id={} outcome=duplicate_submission_blocked", runId);
        }

        SubmitResponse submitResponse = submissionAttempt.submitResponse();
        return RunSubmissionSnapshot.from(submitResponse, duplicateAttempt.duplicateBlocked());
    }

    private String buildRunSummary(RunScoringSnapshot scoringSnapshot, RunSubmissionSnapshot submissionSnapshot) {
        if (scoringSnapshot == null) {
            return null;
        }
        String scoringSummary = scoringSnapshot.getReviewSummary();
        if (submissionSnapshot == null) {
            return scoringSummary;
        }
        return scoringSummary + ", " + submissionSnapshot.getReviewSummary();
    }

    public record StartResult(boolean accepted, String runId, String reason) {

        static StartResult accepted(String runId) {
            return new StartResult(true, runId, null);
        }

        static StartResult rejected(String reason) {
            return new StartResult(false, null, reason);
        }
    }

    private static class RunStatus {
        private final String runId;
        private final String state;
        private final Integer currentPollIndex;
        private final Integer currentAttempt;
        private final int retryCount;
        private final String lastError;
        private final Instant startedAt;
        private final Instant finishedAt;
        private final RunScoringSnapshot scoringSnapshot;
        private final RunSubmissionSnapshot submissionSnapshot;

        private RunStatus(
                String runId,
                String state,
                Integer currentPollIndex,
                Integer currentAttempt,
                int retryCount,
                String lastError,
                Instant startedAt,
                Instant finishedAt,
                RunScoringSnapshot scoringSnapshot,
                RunSubmissionSnapshot submissionSnapshot
        ) {
            this.runId = runId;
            this.state = state;
            this.currentPollIndex = currentPollIndex;
            this.currentAttempt = currentAttempt;
            this.retryCount = retryCount;
            this.lastError = lastError;
            this.startedAt = startedAt;
            this.finishedAt = finishedAt;
            this.scoringSnapshot = scoringSnapshot;
            this.submissionSnapshot = submissionSnapshot;
        }

        static RunStatus idle() {
            return new RunStatus(null, "idle", null, null, 0, null, null, null, null, null);
        }

        static RunStatus running(String runId) {
            return new RunStatus(runId, "running", 0, 1, 0, null, Instant.now(), null, null, null);
        }

        RunStatus withProgress(int pollIndex, int attempt) {
            return new RunStatus(runId, state, pollIndex, attempt, retryCount, lastError, startedAt, finishedAt, scoringSnapshot, submissionSnapshot);
        }

        RunStatus incrementRetryCount() {
            return new RunStatus(runId, state, currentPollIndex, currentAttempt, retryCount + 1, lastError, startedAt, finishedAt, scoringSnapshot, submissionSnapshot);
        }

        RunStatus fail(String error) {
            return new RunStatus(runId, "failed", currentPollIndex, currentAttempt, retryCount, error, startedAt, Instant.now(), scoringSnapshot, submissionSnapshot);
        }

        RunStatus complete(RunScoringSnapshot scoringSnapshot, RunSubmissionSnapshot submissionSnapshot) {
            return new RunStatus(runId, "completed", currentPollIndex, currentAttempt, retryCount, null, startedAt, Instant.now(), scoringSnapshot, submissionSnapshot);
        }
    }
}
