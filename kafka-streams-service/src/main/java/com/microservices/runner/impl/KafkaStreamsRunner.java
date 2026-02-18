package com.microservices.runner.impl;

import com.microservices.avro.model.TwitterAnalyticsAvroModel;
import com.microservices.avro.model.TwitterAvroModel;
import com.microservices.config.KafkaConfigData;
import com.microservices.config.KafkaStreamsConfigData;
import com.microservices.runner.StreamsRunner;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KeyValueMapper;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class KafkaStreamsRunner implements StreamsRunner<String, Long> {
    private final KafkaStreamsConfigData kafkaStreamsConfigData;
    private final KafkaConfigData kafkaConfigData;
    private final Properties streamsConfiguration;

    private KafkaStreams kafkaStreams;
    private volatile ReadOnlyKeyValueStore<String, Long> keyValueStore;

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

        createTopology(twitterAvroModelKStream, serdeConfig);

        startStreaming(streamsBuilder);
    }

    private KStream<Long, TwitterAvroModel> getTwitterAvroModelKStream(Map<String, String> serdeConfig,
                                                                       StreamsBuilder streamsBuilder) {
        final Serde<TwitterAvroModel> serdeTwitterAvroModel = new SpecificAvroSerde<>();
        serdeTwitterAvroModel.configure(serdeConfig, false);

        return streamsBuilder.stream(
                kafkaStreamsConfigData.getInputTopicName(),
                Consumed.with(Serdes.Long(), serdeTwitterAvroModel)
        );
    }

    private void createTopology(KStream<Long, TwitterAvroModel> twitterAvroModelKStream,
                                Map<String, String> serdeConfig) {
        Pattern pattern = Pattern.compile(REGEX, Pattern.UNICODE_CHARACTER_CLASS);

        Serde<TwitterAnalyticsAvroModel> serdeTwitterAnalyticsAvroModel = getSerdeAnalyticsModel(serdeConfig);

        twitterAvroModelKStream
                .flatMapValues(value -> Arrays.asList(pattern.split(value.getText().toLowerCase())))
                .groupBy((key, word) -> word)
                .count(Materialized.as(kafkaStreamsConfigData.getWordCountStoreName()))
                .toStream()
                .map(mapToAnalyticsModel())
                .to(kafkaStreamsConfigData.getOutputTopicName(),
                        Produced.with(Serdes.String(), serdeTwitterAnalyticsAvroModel));
    }

    private Serde<TwitterAnalyticsAvroModel> getSerdeAnalyticsModel(Map<String, String> serdeConfig) {
        Serde<TwitterAnalyticsAvroModel> serdeTwitterAnalyticsAvroModel = new SpecificAvroSerde<>();
        serdeTwitterAnalyticsAvroModel.configure(serdeConfig, false);

        return serdeTwitterAnalyticsAvroModel;
    }

    private KeyValueMapper<String, Long, KeyValue<? extends String, ? extends TwitterAnalyticsAvroModel>> mapToAnalyticsModel() {
        return (word, count) -> {
            LOGGER.info("Sending to topic {}, word {} - count {}",
                    kafkaStreamsConfigData.getOutputTopicName(), word, count);

            return new KeyValue<>(word, TwitterAnalyticsAvroModel
                    .newBuilder()
                    .setWord(word)
                    .setWordCount(count)
                    .setCreatedAt(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                    .build());
        };
    }

    private void startStreaming(StreamsBuilder streamsBuilder) {
        final Topology topology = streamsBuilder.build();

        LOGGER.info("Defined topology: {}", topology.describe());

        kafkaStreams = new KafkaStreams(topology, streamsConfiguration);
        kafkaStreams.start();

        LOGGER.info("Kafka streaming started...");
    }

    @Override
    public Long getValueByKey(String word) {
        if (kafkaStreams != null && kafkaStreams.state() == KafkaStreams.State.RUNNING) {
            if (keyValueStore == null) {
                synchronized (this) {
                    if (keyValueStore == null) {
                        keyValueStore = kafkaStreams.store(
                            StoreQueryParameters.fromNameAndType(
                                    kafkaStreamsConfigData.getWordCountStoreName(),
                                    QueryableStoreTypes.keyValueStore()
                            )
                        );
                    }
                }
            }
            return keyValueStore.get(word.toLowerCase());
        }
        return 0L;
    }

    @PreDestroy
    public void close() {
        if (kafkaStreams != null) {
            kafkaStreams.close();

            LOGGER.info("Kafka streaming closed!");
        }
    }
}