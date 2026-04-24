package com.bajaj.quizvalidator;

import com.bajaj.quizvalidator.config.ValidatorProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ValidatorProperties.class)
public class QuizValidatorApplication {

    public static void main(String[] args) {
        SpringApplication.run(QuizValidatorApplication.class, args);
    }
}
