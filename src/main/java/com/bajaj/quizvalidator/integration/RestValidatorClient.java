package com.bajaj.quizvalidator.integration;

import com.bajaj.quizvalidator.config.ValidatorProperties;
import com.bajaj.quizvalidator.integration.dto.PollResponse;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Component;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Component
public class RestValidatorClient implements ValidatorClient {

    private final ValidatorProperties validatorProperties;
    private final RestTemplate restTemplate;

    public RestValidatorClient(ValidatorProperties validatorProperties, RestTemplateBuilder restTemplateBuilder) {
        this.validatorProperties = validatorProperties;
        this.restTemplate = restTemplateBuilder
                .requestFactory(() -> {
                    SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
                    requestFactory.setConnectTimeout(validatorProperties.getConnectTimeoutMs());
                    requestFactory.setReadTimeout(validatorProperties.getReadTimeoutMs());
                    return requestFactory;
                })
                .build();
    }

    @Override
    public PollResponse fetchMessages(String regNo, int pollIndex) {
        String url = UriComponentsBuilder
                .fromHttpUrl(validatorProperties.getBaseUrl())
                .pathSegment("quiz", "messages")
                .queryParam("regNo", regNo)
                .queryParam("poll", pollIndex)
                .toUriString();

        return restTemplate.getForObject(url, PollResponse.class);
    }
}
