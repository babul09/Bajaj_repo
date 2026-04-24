package com.bajaj.quizvalidator.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
public class AsyncConfig {

    @Bean(name = "runExecutor")
    public Executor runExecutor() {
        return Executors.newSingleThreadExecutor();
    }
}
