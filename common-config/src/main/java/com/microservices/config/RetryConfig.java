package com.microservices.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

@Configuration
@RequiredArgsConstructor
public class RetryConfig {
    private final RetryConfigData configData;

    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();

        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(configData.getInitialIntervalMs());
        backOffPolicy.setMaxInterval(configData.getMaxIntervalMs());
        backOffPolicy.setMultiplier(configData.getMultiplayer());

        retryTemplate.setBackOffPolicy(backOffPolicy);

        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(configData.getMaxAttempts());

        retryTemplate.setRetryPolicy(retryPolicy);

        return retryTemplate;
    }
}