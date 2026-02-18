package com.microservices.runner.impl;

import com.microservices.config.KafkaConfigData;
import com.microservices.config.KafkaStreamsConfigData;
import com.microservices.runner.StreamsRunner;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class KafkaStreamsRunner implements StreamsRunner<String, Long> {
    private final KafkaStreamsConfigData kafkaStreamsConfigData;
    private final KafkaConfigData kafkaConfigData;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsRunner.class);

    @Override
    public void start() {

    }

    @Override
    public Long getValueByKey(String key) {
        return StreamsRunner.super.getValueByKey(key);
    }
}