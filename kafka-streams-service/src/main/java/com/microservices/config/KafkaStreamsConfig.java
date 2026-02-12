package com.microservices.config;

import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig;
import lombok.AllArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
@AllArgsConstructor
public class KafkaStreamsConfig {
    private final KafkaConfigData kafkaConfigData;
    private final KafkaStreamsConfigData kafkaStreamsConfigData;

    @Bean
    @Qualifier("streamConfiguration")
    public Properties streamsConfiguration() {
        Properties streamsConfiguration = new Properties();

        streamsConfiguration.put(StreamsConfig.APPLICATION_ID_CONFIG, kafkaStreamsConfigData.getApplicationId());
        streamsConfiguration.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
        streamsConfiguration.put(
                AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
                kafkaConfigData.getSchemaRegistryUrl()
        );
        streamsConfiguration.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        streamsConfiguration.put(StreamsConfig.STATE_DIR_CONFIG, kafkaStreamsConfigData.getStateFileLocation());

        return streamsConfiguration;
    }
}