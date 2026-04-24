package com.bajaj.quizvalidator.application;

import com.bajaj.quizvalidator.config.ValidatorProperties;
import com.bajaj.quizvalidator.domain.scoring.ScoringEngine;
import com.bajaj.quizvalidator.domain.scoring.LeaderboardEntry;
import com.bajaj.quizvalidator.integration.ValidatorClient;
import com.bajaj.quizvalidator.integration.dto.PollResponse;
import com.bajaj.quizvalidator.integration.dto.SubmitResponse;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunOrchestratorSequenceTest {

    @Test
    void executesExactTenPollIndicesInOrder() {
        List<Integer> observedPollIndices = new ArrayList<>();

        ValidatorClient validatorClient = new ValidatorClient() {
            @Override
            public PollResponse fetchMessages(String regNo, int pollIndex) {
                observedPollIndices.add(pollIndex);
                PollResponse pollResponse = new PollResponse();
                pollResponse.setEvents(List.of());
                return pollResponse;
            }

            @Override
            public SubmitResponse submitLeaderboard(String regNo, List<LeaderboardEntry> leaderboard) {
                SubmitResponse submitResponse = new SubmitResponse();
                submitResponse.setRegNo(regNo);
                submitResponse.setSubmittedTotal(leaderboard.stream().mapToInt(LeaderboardEntry::totalScore).sum());
                submitResponse.setAttemptCount(1);
                return submitResponse;
            }
        };

        ValidatorProperties properties = new ValidatorProperties();
        properties.setMinPollIntervalMs(1);
        properties.setRetryMaxAttempts(3);
        properties.setBaseUrl("https://example.com");
        properties.setConnectTimeoutMs(1000);
        properties.setReadTimeoutMs(1000);

        RunOrchestrator runOrchestrator = new RunOrchestrator(
            validatorClient,
            properties,
            new ScoringEngine(),
            Executors.newSingleThreadExecutor()
        );
        runOrchestrator.initializePolicies();

        runOrchestrator.executeRunBlockingForTest("test-run", "2024CS101");

        assertEquals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), observedPollIndices);
    }
}
