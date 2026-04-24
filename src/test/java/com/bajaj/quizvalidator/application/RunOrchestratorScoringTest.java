package com.bajaj.quizvalidator.application;

import com.bajaj.quizvalidator.api.dto.RunStatusResponse;
import com.bajaj.quizvalidator.config.ValidatorProperties;
import com.bajaj.quizvalidator.domain.scoring.LeaderboardEntry;
import com.bajaj.quizvalidator.domain.scoring.ParticipantTotal;
import com.bajaj.quizvalidator.domain.scoring.ScoringEngine;
import com.bajaj.quizvalidator.domain.scoring.ScoringResult;
import com.bajaj.quizvalidator.integration.ValidatorClient;
import com.bajaj.quizvalidator.integration.dto.PollResponse;
import com.bajaj.quizvalidator.integration.dto.SubmitResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RunOrchestratorScoringTest {

    @Test
    void scoresCollectedPollEventsAfterPollingCompletes() {
        List<PollResponse> pollResponses = new ArrayList<>();
        AtomicBoolean sawFinalPollBeforeScoring = new AtomicBoolean(false);
        AtomicInteger submitCalls = new AtomicInteger();

        ValidatorClient validatorClient = new ValidatorClient() {
            @Override
            public PollResponse fetchMessages(String regNo, int pollIndex) {
                PollResponse pollResponse = new PollResponse();
                pollResponse.setPollIndex(pollIndex);
                switch (pollIndex) {
                    case 0 -> pollResponse.setEvents(List.of(
                            event("round-1", "alice", 10),
                            event("round-2", "bob", 7)
                    ));
                    case 1 -> pollResponse.setEvents(List.of(
                            event("round-1", "alice", 10),
                            event("round-3", "alice", 5)
                    ));
                    default -> pollResponse.setEvents(List.of());
                }
                pollResponses.add(pollResponse);
                if (pollIndex == 9) {
                    sawFinalPollBeforeScoring.set(true);
                }
                return pollResponse;
            }

            @Override
            public SubmitResponse submitLeaderboard(String regNo, List<LeaderboardEntry> leaderboard) {
                submitCalls.incrementAndGet();
                SubmitResponse submitResponse = new SubmitResponse();
                submitResponse.setRegNo(regNo);
                submitResponse.setTotalPollsMade(10);
                submitResponse.setSubmittedTotal(leaderboard.stream().mapToInt(LeaderboardEntry::totalScore).sum());
                submitResponse.setExpectedTotal(leaderboard.stream().mapToInt(LeaderboardEntry::totalScore).sum());
                submitResponse.setAttemptCount(1);
                submitResponse.setIsCorrect(Boolean.TRUE);
                submitResponse.setIsIdempotent(Boolean.TRUE);
                submitResponse.setMessage("accepted");
                return submitResponse;
            }
        };

        TrackingScoringEngine scoringEngine = new TrackingScoringEngine(sawFinalPollBeforeScoring);
        ScoringResult expected = new ScoringEngine().score(pollResponsesForExpectation());

        ValidatorProperties properties = new ValidatorProperties();
        properties.setMinPollIntervalMs(1);
        properties.setRetryMaxAttempts(3);
        properties.setBaseUrl("https://example.com");
        properties.setConnectTimeoutMs(1000);
        properties.setReadTimeoutMs(1000);

        RunOrchestrator runOrchestrator = new RunOrchestrator(
                validatorClient,
                properties,
                scoringEngine,
                Executors.newSingleThreadExecutor()
        );
        runOrchestrator.initializePolicies();

        runOrchestrator.executeRunBlockingForTest("scoring-run", "2024CS101");

        RunStatusResponse status = runOrchestrator.currentStatus();
        assertEquals("completed", status.getState());
        assertEquals(expected.uniqueEventCount(), status.getUniqueEventCount());
        assertEquals(expected.duplicateEventCount(), status.getDuplicateEventCount());
        assertEquals(expected.combinedTotalScore(), status.getCombinedTotalScore());
        assertEquals(expected.participantTotals().size(), status.getParticipantCount());
        assertEquals(expected.leaderboard().size(), status.getLeaderboardSize());
        assertEquals(expected.participantTotals(), status.getParticipantTotals());
        assertEquals(expected.leaderboard(), status.getLeaderboard());
        assertEquals(Boolean.TRUE, status.getSubmissionAttempted());
        assertEquals(Boolean.TRUE, status.getDuplicateSubmissionBlocked());
        assertEquals(10, status.getTotalPollsMade());
        assertEquals(expected.combinedTotalScore(), status.getSubmittedTotal());
        assertEquals(expected.combinedTotalScore(), status.getExpectedTotal());
        assertEquals(1, status.getSubmitAttemptCount());
        assertEquals(Boolean.TRUE, status.getIsCorrect());
        assertEquals(Boolean.TRUE, status.getIsIdempotent());
        assertEquals("accepted", status.getSubmitMessage());
        assertTrue(status.getScoringSummary().contains("uniqueEvents=3"));
        assertTrue(status.getScoringSummary().contains("duplicates=1"));
        assertTrue(status.getScoringSummary().contains("participants=2"));
        assertTrue(status.getScoringSummary().contains("leaderboard=[alice=15, bob=7]"));
        assertTrue(status.getRunSummary().contains("submittedTotal=22"));
        assertTrue(status.getRunSummary().contains("duplicateSubmissionBlocked=true"));
        assertEquals(1, scoringEngine.invocations);
        assertEquals(1, submitCalls.get());
        assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), pollResponses.stream().map(PollResponse::getPollIndex).toList());
        assertEquals(10, scoringEngine.scoredPollCount);
        assertTrue(scoringEngine.scoredAfterFinalPoll);
    }

    private static List<PollResponse> pollResponsesForExpectation() {
        List<PollResponse> pollResponses = new ArrayList<>();
        for (int pollIndex = 0; pollIndex <= 9; pollIndex++) {
            PollResponse pollResponse = new PollResponse();
            pollResponse.setPollIndex(pollIndex);
            switch (pollIndex) {
                case 0 -> pollResponse.setEvents(List.of(
                        event("round-1", "alice", 10),
                        event("round-2", "bob", 7)
                ));
                case 1 -> pollResponse.setEvents(List.of(
                        event("round-1", "alice", 10),
                        event("round-3", "alice", 5)
                ));
                default -> pollResponse.setEvents(List.of());
            }
            pollResponses.add(pollResponse);
        }
        return pollResponses;
    }

    private static PollResponse.PollEvent event(String roundId, String participant, int score) {
        PollResponse.PollEvent pollEvent = new PollResponse.PollEvent();
        pollEvent.setRoundId(roundId);
        pollEvent.setParticipant(participant);
        pollEvent.setScore(score);
        return pollEvent;
    }

    private static final class TrackingScoringEngine extends ScoringEngine {
        private final AtomicBoolean sawFinalPollBeforeScoring;
        private int invocations;
        private int scoredPollCount;
        private boolean scoredAfterFinalPoll;

        private TrackingScoringEngine(AtomicBoolean sawFinalPollBeforeScoring) {
            this.sawFinalPollBeforeScoring = sawFinalPollBeforeScoring;
        }

        @Override
        public ScoringResult score(List<PollResponse> pollResponses) {
            invocations++;
            scoredPollCount = pollResponses.size();
            scoredAfterFinalPoll = sawFinalPollBeforeScoring.get();
            return super.score(pollResponses);
        }
    }
}
