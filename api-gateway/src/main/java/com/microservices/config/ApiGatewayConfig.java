package com.microservices.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.circuitbreaker.resilience4j.ReactiveResilience4JCircuitBreakerFactory;
import org.springframework.cloud.circuitbreaker.resilience4j.Resilience4JConfigBuilder;
import org.springframework.cloud.client.circuitbreaker.Customizer;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Objects;

@Configuration
@RequiredArgsConstructor
public class ApiGatewayConfig {
    private final ApiGatewayConfigData gatewayConfigData;

    private static final String HEADER_FOR_KEY_RESOLVER = "Authorization";

    @Bean(name = "authHeaderResolver")
    KeyResolver userKeyResolver() {
        return exchange -> Mono.just(Objects.requireNonNull(
                exchange.getRequest().getHeaders().getFirst(HEADER_FOR_KEY_RESOLVER)
        ));
    }

    @Bean
    Customizer<ReactiveResilience4JCircuitBreakerFactory> circuitBreakerFactoryCustomizer() {
        return circuitBreakerFactory -> circuitBreakerFactory
                .configureDefault(id -> new Resilience4JConfigBuilder(id)
                        .timeLimiterConfig(TimeLimiterConfig.custom()
                                .timeoutDuration(Duration.ofMillis(gatewayConfigData.getTimeoutMs()))
                                .build())
                        .circuitBreakerConfig(CircuitBreakerConfig.custom()
                                .failureRateThreshold(gatewayConfigData.getFailureRateThreshold())
                                .slowCallRateThreshold(gatewayConfigData.getSlowCallRateThreshold())
                                .slowCallDurationThreshold(
                                        Duration.ofMillis(gatewayConfigData.getSlowCallDurationThreshold())
                                )
                                .permittedNumberOfCallsInHalfOpenState(
                                        gatewayConfigData.getPermittedNumOfCallsInHalfOpenSTate()
                                )
                                .slidingWindowSize(gatewayConfigData.getSlidingWindowSize())
                                .minimumNumberOfCalls(gatewayConfigData.getMinNumberOfCalls())
                                .waitDurationInOpenState(
                                        Duration.ofMillis(gatewayConfigData.getWaitDurationInOpenState())
                                )
                                .build())
                        .build());
    }
}