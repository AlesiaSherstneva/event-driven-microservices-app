package com.microservices.runner.impl;

import com.microservices.avro.model.TwitterAvroModel;
import com.microservices.config.KafkaConfigData;
import com.microservices.config.KafkaStreamsConfigData;
import com.microservices.runner.StreamsRunner;
import lombok.AllArgsConstructor;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.KStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

@Component
@AllArgsConstructor
public class KafkaStreamsRunner implements StreamsRunner<String, Long> {
    private final KafkaStreamsConfigData kafkaStreamsConfigData;
    private final KafkaConfigData kafkaConfigData;
    private final Properties streamsConfiguration;

    private static final String REGEX = "\\W+";

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsRunner.class);

    @Override
    public void start() {
        final Map<String, String> serdeConfig = Collections.singletonMap(
            kafkaConfigData.getSchemaRegistryUrlKey(),
            kafkaConfigData.getSchemaRegistryUrl()
        );

        final StreamsBuilder streamsBuilder = new StreamsBuilder();

        final KStream<Long, TwitterAvroModel> twitterAvroModelKStream =
                getTwitterAvroModelKStream(serdeConfig, streamsBuilder);
    }

    private KStream<Long, TwitterAvroModel> getTwitterAvroModelKStream(Map<String, String> serdeConfig,
                                                                       StreamsBuilder streamsBuilder) {

    }

    @Override
    public Long getValueByKey(String key) {
        return StreamsRunner.super.getValueByKey(key);
    }
}