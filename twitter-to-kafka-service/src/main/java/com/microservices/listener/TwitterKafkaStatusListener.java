package com.microservices.listener;

import com.microservices.avro.model.TwitterAvroModel;
import com.microservices.config.KafkaConfigData;
import com.microservices.service.KafkaProducer;
import com.microservices.transformer.TwitterStatusToAvroTransformer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.StatusAdapter;

@Component
@RequiredArgsConstructor
public class TwitterKafkaStatusListener extends StatusAdapter {
    private final KafkaConfigData kafkaConfigData;
    private final KafkaProducer<Long, TwitterAvroModel> kafkaProducer;
    private final TwitterStatusToAvroTransformer transformer;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterKafkaStatusListener.class);

    @Override
    public void onStatus(Status status) {
        LOGGER.info("Received status text '{}' sending to kafka topic '{}'",
                status.getText(), kafkaConfigData.getTopicName());

        TwitterAvroModel avroModel = transformer.getTwitterAvroModelFromStatus(status);
        kafkaProducer.send(kafkaConfigData.getTopicName(), avroModel.getUserId(), avroModel);
    }
}