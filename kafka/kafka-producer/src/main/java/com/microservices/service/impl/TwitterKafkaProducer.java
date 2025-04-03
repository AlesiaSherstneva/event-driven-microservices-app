package com.microservices.service.impl;

import com.microservices.avro.model.TwitterAvroModel;
import com.microservices.service.KafkaProducer;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
public class TwitterKafkaProducer implements KafkaProducer<Long, TwitterAvroModel> {
    private final KafkaTemplate<Long, TwitterAvroModel> kafkaTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterKafkaProducer.class);

    @Override
    public void send(String topicName, Long key, TwitterAvroModel message) {
        LOGGER.info("Sending message='{}' to topic='{}'", message, topicName);

        CompletableFuture<SendResult<Long, TwitterAvroModel>> kafkaResultFuture
                = kafkaTemplate.send(topicName, key, message);
        kafkaResultFuture.whenComplete((result, ex) -> {
            if (ex == null) {
                RecordMetadata metadata = result.getRecordMetadata();
                LOGGER.debug("Received new metadata. Topic: {}; Partition: {}; Offset: {}; Timestamp: {}; at time: {}",
                        metadata.topic(),
                        metadata.partition(),
                        metadata.offset(),
                        metadata.timestamp(),
                        System.nanoTime());
            } else {
                LOGGER.error("Error while sending message {} to topic {}", message.toString(), topicName, ex);
            }
        });
    }

    @PreDestroy
    public void close() {
        if (kafkaTemplate != null) {
            LOGGER.info("Closing kafka producer!");
            kafkaTemplate.destroy();
        }
    }
}