package com.bajaj.quizvalidator.domain.scoring;

import com.bajaj.quizvalidator.integration.dto.PollResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class ScoringEngine {

    public ScoringResult score(List<PollResponse> pollResponses) {
        Objects.requireNonNull(pollResponses, "pollResponses must be provided");

        List<ScoringEvent> scoringEvents = new ArrayList<>();
        for (PollResponse pollResponse : pollResponses) {
            if (pollResponse == null) {
                throw new IllegalArgumentException("pollResponse must not be null");
            }
            if (pollResponse.getEvents() == null) {
                throw new IllegalArgumentException("pollResponse events must be provided");
            }
            for (PollResponse.PollEvent pollEvent : pollResponse.getEvents()) {
                scoringEvents.add(ScoringEvent.from(pollEvent));
            }
        }

        return scoreEvents(scoringEvents);
    }

    public ScoringResult scoreEvents(List<ScoringEvent> scoringEvents) {
        Objects.requireNonNull(scoringEvents, "scoringEvents must be provided");

        Map<ScoringKey, ScoringEvent> uniqueEvents = new LinkedHashMap<>();
        int duplicateCount = 0;
        for (ScoringEvent scoringEvent : scoringEvents) {
            Objects.requireNonNull(scoringEvent, "scoringEvent must not be null");
            ScoringKey key = new ScoringKey(scoringEvent.roundId(), scoringEvent.participant());
            if (uniqueEvents.containsKey(key)) {
                duplicateCount++;
                continue;
            }
            uniqueEvents.put(key, scoringEvent);
        }

        Map<String, Integer> participantTotalsByName = new LinkedHashMap<>();
        for (ScoringEvent scoringEvent : uniqueEvents.values()) {
            participantTotalsByName.merge(scoringEvent.participant(), scoringEvent.score(), Integer::sum);
        }

        List<ParticipantTotal> participantTotals = participantTotalsByName.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new ParticipantTotal(entry.getKey(), entry.getValue()))
                .toList();

        List<LeaderboardEntry> leaderboard = participantTotals.stream()
                .map(participantTotal -> new LeaderboardEntry(participantTotal.participant(), participantTotal.totalScore()))
                .sorted(Comparator.comparingInt(LeaderboardEntry::totalScore).reversed()
                        .thenComparing(LeaderboardEntry::participant))
                .toList();

        int combinedTotalScore = leaderboard.stream().mapToInt(LeaderboardEntry::totalScore).sum();

        return new ScoringResult(
                participantTotals,
                leaderboard,
                uniqueEvents.size(),
                duplicateCount,
                combinedTotalScore
        );
    }

    private record ScoringKey(String roundId, String participant) {
    }
}