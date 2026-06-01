package com.microservices.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class WebSecurityConfig {
    @Bean
    public SecurityWebFilterChain webFluxSecurityConfig(ServerHttpSecurity http) {
        http.authorizeExchange(exchanges -> exchanges
                        .anyExchange()
                        .permitAll())
                .csrf(ServerHttpSecurity.CsrfSpec::disable);

        return http.build();
    }
}