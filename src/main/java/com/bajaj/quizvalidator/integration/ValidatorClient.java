package com.bajaj.quizvalidator.integration;

import com.bajaj.quizvalidator.domain.scoring.LeaderboardEntry;
import com.bajaj.quizvalidator.integration.dto.PollResponse;
import com.bajaj.quizvalidator.integration.dto.SubmitResponse;

import java.util.List;

public interface ValidatorClient {
    PollResponse fetchMessages(String regNo, int pollIndex);

    SubmitResponse submitLeaderboard(String regNo, List<LeaderboardEntry> leaderboard);
}
