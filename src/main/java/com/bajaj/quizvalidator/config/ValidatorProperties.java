package com.bajaj.quizvalidator.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "validator")
public class ValidatorProperties {

    private String baseUrl;
    private int connectTimeoutMs;
    private int readTimeoutMs;
    private int minPollIntervalMs;
    private int retryMaxAttempts;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public void setConnectTimeoutMs(int connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public void setReadTimeoutMs(int readTimeoutMs) {
        this.readTimeoutMs = readTimeoutMs;
    }

    public int getMinPollIntervalMs() {
        return minPollIntervalMs;
    }

    public void setMinPollIntervalMs(int minPollIntervalMs) {
        this.minPollIntervalMs = minPollIntervalMs;
    }

    public int getRetryMaxAttempts() {
        return retryMaxAttempts;
    }

    public void setRetryMaxAttempts(int retryMaxAttempts) {
        this.retryMaxAttempts = retryMaxAttempts;
    }
}
