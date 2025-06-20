package com.microservices.consumer.impl;

import com.microservices.avro.model.TwitterAvroModel;
import com.microservices.client.KafkaAdminClient;
import com.microservices.config.KafkaConfigData;
import com.microservices.config.KafkaConsumerConfigData;
import com.microservices.consumer.KafkaConsumer;
import com.microservices.elastic.model.impl.TwitterIndexModel;
import com.microservices.service.ElasticIndexClient;
import com.microservices.transformer.AvroToElasticModelTransformer;
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
public class TwitterKafkaConsumer implements KafkaConsumer<Long, TwitterAvroModel> {
    private final KafkaListenerEndpointRegistry endpointRegistry;
    private final KafkaAdminClient adminClient;
    private final KafkaConfigData configData;
    private final KafkaConsumerConfigData consumerConfigData;
    private final AvroToElasticModelTransformer transformer;
    private final ElasticIndexClient<TwitterIndexModel> indexClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterKafkaConsumer.class);

    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted() {
        adminClient.checkIfTopicsCreated();

        LOGGER.info("Topics with names {} are ready for operations", configData.getTopicNamesToCreate().toArray());

        endpointRegistry.getListenerContainer(consumerConfigData.getConsumerGroupId()).start();
    }

    @Override
    @KafkaListener(id = "${kafka-consumer-config.consumer-group-id}", topics = "${kafka-config.topic-name}")
    public void receive(@Payload List<TwitterAvroModel> messages,
                        @Header(KafkaHeaders.RECEIVED_KEY) List<Integer> keys,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) List<Integer> partitions,
                        @Header(KafkaHeaders.OFFSET) List<Long> offsets) {
        LOGGER.info("{} number of messages received with keys {}, partitions {} and offsets {}, " +
                        "sending it to elastic: Thread id {}",
                messages.size(), keys.toString(), partitions.toString(),
                offsets.toString(), Thread.currentThread().getId());

        List<TwitterIndexModel> twitterIndexModels = transformer.getElasticModels(messages);
        List<String> documentIds = indexClient.save(twitterIndexModels);

        LOGGER.info("Documents saved to elasticsearch with ids: {}", documentIds);
    }
}