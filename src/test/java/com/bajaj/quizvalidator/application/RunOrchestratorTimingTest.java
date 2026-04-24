package com.bajaj.quizvalidator.application;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RunOrchestratorTimingTest {

    @Test
    void enforcesMinimumIntervalBetweenConsecutivePollStarts() {
        MutableClock clock = new MutableClock(Instant.parse("2026-04-24T12:00:00Z"));
        List<Long> sleptMillis = new ArrayList<>();

        PollDelayEnforcer enforcer = new PollDelayEnforcer(Duration.ofMillis(5000), clock, sleptMillis::add);

        enforcer.enforceFrom(null);
        clock.advanceMillis(1000);
        enforcer.enforceFrom(Instant.parse("2026-04-24T12:00:00Z"));
        clock.advanceMillis(2000);
        enforcer.enforceFrom(Instant.parse("2026-04-24T12:00:01Z"));

        assertEquals(List.of(4001L, 3001L), sleptMillis);
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void advanceMillis(long millis) {
            instant = instant.plusMillis(millis);
        }
    }
}
