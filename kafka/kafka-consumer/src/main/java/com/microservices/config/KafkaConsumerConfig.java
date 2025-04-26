package com.microservices.config;

import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig<K extends Serializable, V extends SpecificRecordBase> {
    private final KafkaConfigData kafkaConfigData;
    private final KafkaConsumerConfigData consumerConfigData;

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, consumerConfigData.getKeyDeserializer());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, consumerConfigData.getValueDeserializer());
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, consumerConfigData.getConsumerGroupId());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, consumerConfigData.getAutoOffsetReset());
        properties.put(kafkaConfigData.getSchemaRegistryUrlKey(), kafkaConfigData.getSchemaRegistryUrl());
        properties.put(consumerConfigData.getSpecificAvroReaderKey(), consumerConfigData.getSpecificAvroReader());
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, consumerConfigData.getSessionTimeoutMs());
        properties.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, consumerConfigData.getHeartbeatIntervalMs());
        properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, consumerConfigData.getMaxPollIntervalMs());
        properties.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG,
                consumerConfigData.getMaxPartitionFetchBytesDefault() * consumerConfigData.getMaxPartitionFetchBytesBoostFactor());
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, consumerConfigData.getMaxPollRecords());

        return properties;
    }

    @Bean
    public ConsumerFactory<K, V> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<K, V>> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<K, V> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        factory.setBatchListener(consumerConfigData.getBatchListener());
        factory.setConcurrency(consumerConfigData.getConcurrencyLevel());
        factory.setAutoStartup(consumerConfigData.getAutoStartup());
        factory.getContainerProperties().setPollTimeout(consumerConfigData.getPollTimeoutMs());

        return factory;
    }
}