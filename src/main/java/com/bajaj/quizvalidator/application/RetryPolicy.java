package com.bajaj.quizvalidator.application;

import org.springframework.http.HttpStatusCode;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

public class RetryPolicy {

    private final int maxAttempts;

    public RetryPolicy(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public boolean isRetryable(Throwable throwable) {
        if (throwable instanceof ResourceAccessException) {
            return true;
        }
        if (throwable instanceof HttpStatusCodeException statusCodeException) {
            HttpStatusCode statusCode = statusCodeException.getStatusCode();
            return statusCode.is5xxServerError();
        }
        return false;
    }
}
