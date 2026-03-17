package com.microservices.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Data
@Configuration
@ConfigurationProperties(prefix = "elastic-query-service")
@Profile({"elastic_query", "elastic_query_reactive"})
public class ElasticQueryServiceConfigData {
    private String version;
    private String customAudience;
    private Long backPressureDelayMs;
    private WebClient webClient;
    private Query queryFromKafkaStateStore;

    @Data
    public static class WebClient {
        private Integer connectTimeoutMs;
        private Integer readTimeoutMs;
        private Integer writeTimeoutMs;
        private Integer maxInMemorySize;
        private String contentType;
        private String acceptType;
        private String queryType;
    }

    @Bean
    public WebClient webClient() {
        return webClient;
    }

    @Data
    public static class Query {
        private String method;
        private String accept;
        private String uri;
    }
}