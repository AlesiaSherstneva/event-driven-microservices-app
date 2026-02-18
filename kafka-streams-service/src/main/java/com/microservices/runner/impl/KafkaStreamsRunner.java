package com.microservices.runner.impl;

import com.microservices.runner.StreamsRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class KafkaStreamsRunner implements StreamsRunner<String, Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsRunner.class);

    @Override
    public void start() {

    }

    @Override
    public Long getValueByKey(String key) {
        return StreamsRunner.super.getValueByKey(key);
    }
}