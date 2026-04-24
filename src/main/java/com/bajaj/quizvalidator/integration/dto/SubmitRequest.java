package com.bajaj.quizvalidator.integration.dto;

import com.bajaj.quizvalidator.domain.scoring.LeaderboardEntry;

import java.util.List;

public class SubmitRequest {

    private final String regNo;
    private final List<LeaderboardEntry> leaderboard;

    public SubmitRequest(String regNo, List<LeaderboardEntry> leaderboard) {
        this.regNo = regNo;
        this.leaderboard = List.copyOf(leaderboard);
    }

    public String getRegNo() {
        return regNo;
    }

    public List<LeaderboardEntry> getLeaderboard() {
        return leaderboard;
    }
}
