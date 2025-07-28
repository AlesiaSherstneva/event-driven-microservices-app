package com.microservices.reactive.service.impl;

import com.microservices.config.ElasticQueryServiceConfigData;
import com.microservices.elastic.model.impl.TwitterIndexModel;
import com.microservices.reactive.repository.ElasticQueryRepository;
import com.microservices.reactive.service.ElasticQueryClientReactive;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TwitterElasticQueryClientReactive implements ElasticQueryClientReactive<TwitterIndexModel> {
    private final ElasticQueryRepository repository;
    private final ElasticQueryServiceConfigData configData;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterElasticQueryClientReactive.class);

    @Override
    public Flux<TwitterIndexModel> getIndexModelByText(String text) {
        LOGGER.info("Getting data from elasticsearch for text: {}", text);

        return repository
                .findByText(text)
                .delayElements(Duration.ofMillis(configData.getBackPressureDelayMs()));
    }
}