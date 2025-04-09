package com.microservices.init.impl;

import com.microservices.client.KafkaAdminClient;
import com.microservices.config.KafkaConfigData;
import com.microservices.init.StreamInitializer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaStreamInitializer implements StreamInitializer {
    private final KafkaConfigData configData;
    private final KafkaAdminClient adminClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamInitializer.class);

    @Override
    public void init() {
        adminClient.createTopics();
        adminClient.checkSchemaRegistry();

        LOGGER.info("Topics {} are ready for operations", configData.getTopicNamesToCreate().toArray());
    }
}