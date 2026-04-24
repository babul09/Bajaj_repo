package com.bajaj.quizvalidator.application;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

public class PollDelayEnforcer {

    public interface Sleeper {
        void sleep(long millis) throws InterruptedException;
    }

    private final Duration minInterval;
    private final Clock clock;
    private final Sleeper sleeper;

    public PollDelayEnforcer(Duration minInterval) {
        this(minInterval, Clock.systemUTC(), Thread::sleep);
    }

    public PollDelayEnforcer(Duration minInterval, Clock clock, Sleeper sleeper) {
        this.minInterval = minInterval;
        this.clock = clock;
        this.sleeper = sleeper;
    }

    public void enforceFrom(Instant previousRequestStart) {
        if (previousRequestStart == null) {
            return;
        }
        Instant nextAllowed = previousRequestStart.plus(minInterval);
        Instant now = clock.instant();
        if (now.isBefore(nextAllowed)) {
            long waitMs = Duration.between(now, nextAllowed).toMillis() + 1;
            try {
                sleeper.sleep(waitMs);
            } catch (InterruptedException interruptedException) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting poll interval", interruptedException);
            }
        }
    }
}
