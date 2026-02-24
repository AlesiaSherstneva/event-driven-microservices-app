package com.microservices.init.impl;

import com.microservices.client.KafkaAdminClient;
import com.microservices.config.KafkaConfigData;
import com.microservices.init.StreamsInitializer;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class KafkaStreamsInitializer implements StreamsInitializer {
    private final KafkaConfigData kafkaConfigData;
    private final KafkaAdminClient kafkaAdminClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsInitializer.class);

    @Override
    public void init() {
        kafkaAdminClient.checkIfTopicsCreated();
        kafkaAdminClient.checkSchemaRegistry();

        LOGGER.info("Topics {} are ready for operations", kafkaConfigData.getTopicNamesToCreate());
    }
}