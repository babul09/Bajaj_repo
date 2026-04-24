package com.bajaj.quizvalidator.api.dto;

import jakarta.validation.constraints.NotBlank;

public class StartRunRequest {

    @NotBlank
    private String regNo;

    public String getRegNo() {
        return regNo;
    }

    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }
}
