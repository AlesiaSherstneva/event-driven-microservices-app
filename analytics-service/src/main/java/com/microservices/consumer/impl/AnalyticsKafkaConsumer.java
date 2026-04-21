package com.microservices.consumer.impl;

import com.microservices.avro.model.TwitterAnalyticsAvroModel;
import com.microservices.client.KafkaAdminClient;
import com.microservices.config.KafkaConfigData;
import com.microservices.consumer.KafkaConsumer;
import com.microservices.dataaccess.entity.AnalyticsEntity;
import com.microservices.dataaccess.repository.AnalyticsRepository;
import com.microservices.transformer.AvroToDbEntityModelTransformer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsKafkaConsumer implements KafkaConsumer<TwitterAnalyticsAvroModel> {
    private final KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
    private final KafkaAdminClient kafkaAdminClient;
    private final KafkaConfigData kafkaConfig;
    private final AvroToDbEntityModelTransformer avroToEntityTransformer;
    private final AnalyticsRepository analyticsRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsKafkaConsumer.class);

    @EventListener
    public void onAppStarted(ApplicationStartedEvent event) {
        kafkaAdminClient.checkIfTopicsCreated();

        LOGGER.info("Topics {} are ready for operations", kafkaConfig.getTopicNamesToCreate().toArray());

        kafkaListenerEndpointRegistry.getListenerContainer("twitterAnalyticsTopicListener").start();
    }

    @Override
    @KafkaListener(id = "twitterAnalyticsTopicListener", topics = {"kafka-config.topic-name"}, autoStartup = "false")
    public void receive(@Payload List<TwitterAnalyticsAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<String> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        LOGGER.info("{} number of message received with keys {}, partitions {} and offsets {}, " +
                "sending it to database: Thread id {}", messages.size(), keys.toString(), partitions.toString(),
                offsets.toString(), Thread.currentThread().getId());

        List<AnalyticsEntity> twitterAnalyticsEntities = avroToEntityTransformer.getEntityModels(messages);
        analyticsRepository.batchPersist(twitterAnalyticsEntities);

        LOGGER.info("{} number of messages sent to database", twitterAnalyticsEntities.size());
    }
}