package com.microservices.config;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import zipkin2.reporter.BytesMessageSender;
import zipkin2.reporter.kafka.KafkaSender;

import java.util.Map;

@Configuration
@ConditionalOnClass(KafkaSender.class)
@ConditionalOnProperty(name = "spring.kafka.bootstrap-servers")
public class ZipkinKafkaConfig {
    @Bean
    public BytesMessageSender zipkinKafkaSender(@Value("${spring.kafka.bootstrap-servers}") String bootstrapServers) {
        return KafkaSender.newBuilder()
                .topic("zipkin")
                .overrides(Map.of(
                        ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                        ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName(),
                        ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName()
                ))
                .build();
    }
}