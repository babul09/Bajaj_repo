package com.bajaj.quizvalidator.application;

import com.bajaj.quizvalidator.api.dto.RunStatusResponse;
import com.bajaj.quizvalidator.config.ValidatorProperties;
import com.bajaj.quizvalidator.integration.ValidatorClient;
import com.bajaj.quizvalidator.integration.dto.PollResponse;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.ResourceAccessException;

import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunOrchestratorRetryTest {

    @Test
    void retriesTransientFailuresWithinBoundedAttempts() {
        AtomicInteger attemptsForFirstPoll = new AtomicInteger();

        ValidatorClient validatorClient = (regNo, pollIndex) -> {
            if (pollIndex == 0) {
                int currentAttempt = attemptsForFirstPoll.incrementAndGet();
                if (currentAttempt < 3) {
                    throw new ResourceAccessException("temporary timeout");
                }
            }
            return new PollResponse();
        };

        ValidatorProperties properties = new ValidatorProperties();
        properties.setMinPollIntervalMs(1);
        properties.setRetryMaxAttempts(3);
        properties.setBaseUrl("https://example.com");
        properties.setConnectTimeoutMs(1000);
        properties.setReadTimeoutMs(1000);

        RunOrchestrator runOrchestrator = new RunOrchestrator(validatorClient, properties, Executors.newSingleThreadExecutor());
        runOrchestrator.initializePolicies();

        runOrchestrator.executeRunBlockingForTest("retry-run", "2024CS101");

        RunStatusResponse status = runOrchestrator.currentStatus();
        assertEquals("completed", status.getState());
        assertEquals(2, status.getRetryCount());
        assertEquals(3, attemptsForFirstPoll.get());
    }
}
