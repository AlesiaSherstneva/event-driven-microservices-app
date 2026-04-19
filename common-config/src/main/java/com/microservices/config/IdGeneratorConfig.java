package com.microservices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.util.IdGenerator;
import org.springframework.util.JdkIdGenerator;

@Component
public class IdGeneratorConfig {
    @Bean
    public IdGenerator idGenerator() {
        return new JdkIdGenerator();
    }
}