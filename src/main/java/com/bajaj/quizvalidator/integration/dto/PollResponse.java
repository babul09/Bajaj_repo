package com.bajaj.quizvalidator.integration.dto;

import java.util.List;

public class PollResponse {

    private String regNo;
    private String setId;
    private Integer pollIndex;
    private List<PollEvent> events;

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }

    public String getSetId() {
        return setId;
    }

    public void setSetId(String setId) {
        this.setId = setId;
    }

    public Integer getPollIndex() {
        return pollIndex;
    }

    public void setPollIndex(Integer pollIndex) {
        this.pollIndex = pollIndex;
    }

    public List<PollEvent> getEvents() {
        return events;
    }

    public void setEvents(List<PollEvent> events) {
        this.events = events;
    }

    public static class PollEvent {
        private String roundId;
        private String participant;
        private Integer score;

        public String getRoundId() {
            return roundId;
        }

        public void setRoundId(String roundId) {
            this.roundId = roundId;
        }

        public String getParticipant() {
            return participant;
        }

        public void setParticipant(String participant) {
            this.participant = participant;
        }

        public Integer getScore() {
            return score;
        }

        public void setScore(Integer score) {
            this.score = score;
        }
    }
}
