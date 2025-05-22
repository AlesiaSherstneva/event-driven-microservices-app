package com.microservices.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.lang.NonNull;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class ElasticsearchConfig extends ElasticsearchConfiguration {
    private final ElasticConfigData configData;

    @Bean
    @NonNull
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(configData.getConnectionUrl())
                .withConnectTimeout(Duration.ofMillis(configData.getConnectTimeoutMs()))
                .withSocketTimeout(Duration.ofMillis(configData.getSocketTimeoutMs()))
                .build();
    }
}