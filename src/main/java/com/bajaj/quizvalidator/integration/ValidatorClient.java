package com.bajaj.quizvalidator.integration;

import com.bajaj.quizvalidator.integration.dto.PollResponse;

public interface ValidatorClient {
    PollResponse fetchMessages(String regNo, int pollIndex);
}
