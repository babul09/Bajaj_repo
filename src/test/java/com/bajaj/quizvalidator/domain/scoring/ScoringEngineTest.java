package com.bajaj.quizvalidator.domain.scoring;

import com.bajaj.quizvalidator.integration.dto.PollResponse;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScoringEngineTest {

    private final ScoringEngine scoringEngine = new ScoringEngine();

    @Test
    void deduplicatesByRoundAndParticipantBeforeScoring() {
        PollResponse firstPoll = pollResponse(
                event("round-1", "alice", 10),
                event("round-2", "bob", 7)
        );
        PollResponse secondPoll = pollResponse(
                event("round-1", "alice", 10),
                event("round-3", "alice", 5)
        );

        ScoringResult result = scoringEngine.score(List.of(firstPoll, secondPoll));

        assertEquals(3, result.uniqueEventCount());
        assertEquals(1, result.duplicateEventCount());
        assertEquals(List.of(
                new ParticipantTotal("alice", 15),
                new ParticipantTotal("bob", 7)
        ), result.participantTotals());
        assertEquals(List.of(
                new LeaderboardEntry("alice", 15),
                new LeaderboardEntry("bob", 7)
        ), result.leaderboard());
        assertEquals(22, result.combinedTotalScore());
    }

    @Test
    void ignoresDuplicateEventsRepeatedAcrossMultiplePollResponses() {
        ScoringResult result = scoringEngine.score(List.of(
                pollResponse(
                        event("round-1", "alice", 10),
                        event("round-2", "bob", 7)
                ),
                pollResponse(
                        event("round-1", "alice", 10),
                        event("round-3", "alice", 5)
                ),
                pollResponse(
                        event("round-2", "bob", 7),
                        event("round-3", "alice", 5)
                )
        ));

        assertEquals(3, result.uniqueEventCount());
        assertEquals(3, result.duplicateEventCount());
        assertEquals(List.of(
                new LeaderboardEntry("alice", 15),
                new LeaderboardEntry("bob", 7)
        ), result.leaderboard());
        assertEquals(22, result.combinedTotalScore());
    }

    @Test
    void sortsLeaderboardDeterministicallyWhenTotalsTie() {
        ScoringResult result = scoringEngine.scoreEvents(List.of(
                new ScoringEvent("round-1", "zoe", 8),
                new ScoringEvent("round-2", "amy", 8),
                new ScoringEvent("round-3", "mike", 3)
        ));

        assertEquals(List.of(
                new LeaderboardEntry("amy", 8),
                new LeaderboardEntry("zoe", 8),
                new LeaderboardEntry("mike", 3)
        ), result.leaderboard());
    }

    @Test
    void rejectsInvalidScoringInputImmediately() {
        assertThrows(IllegalArgumentException.class, () -> new ScoringEvent("round-1", "", 5));
    }

    private static PollResponse pollResponse(PollResponse.PollEvent... events) {
        PollResponse pollResponse = new PollResponse();
        pollResponse.setEvents(List.of(events));
        return pollResponse;
    }

    private static PollResponse.PollEvent event(String roundId, String participant, int score) {
        PollResponse.PollEvent pollEvent = new PollResponse.PollEvent();
        pollEvent.setRoundId(roundId);
        pollEvent.setParticipant(participant);
        pollEvent.setScore(score);
        return pollEvent;
    }
}
