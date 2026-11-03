package com.microservices.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import lombok.RequiredArgsConstructor;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.BatchInterceptor;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.lang.NonNull;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConsumerConfig<K extends Serializable, V extends SpecificRecordBase> {
    private final KafkaConfigData kafkaConfigData;
    private final KafkaConsumerConfigData consumerConfigData;
    private final Tracer tracer;
    private final Propagator propagator;

    private static final String APACHE_KAFKA = "apache kafka";

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
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setBatchInterceptor(createTracingBatchInterceptor());

        return factory;
    }

    private BatchInterceptor<K, V> createTracingBatchInterceptor() {
        return new BatchInterceptor<>() {
            private final ThreadLocal<Span> currentSpan = new ThreadLocal<>();
            private final ThreadLocal<Tracer.SpanInScope> currentScope = new ThreadLocal<>();

            @NonNull
            @Override
            public ConsumerRecords<K, V> intercept(@NonNull ConsumerRecords<K, V> records,
                                                   @NonNull Consumer<K, V> consumer) {
                if (!records.isEmpty()) {
                    ConsumerRecord<K, V> first = records.iterator().next();

                    Map<String, String> headersMap = new HashMap<>();
                    first.headers().forEach(header ->
                            headersMap.put(header.key(), new String(header.value(), StandardCharsets.UTF_8))
                    );

                    Span span = propagator.extract(headersMap, Map::get)
                            .name("kafka.consume.batch")
                            .kind(Span.Kind.CONSUMER)
                            .remoteServiceName(resolveKafkaRemoteServiceName())
                            .start();

                    currentSpan.set(span);
                    currentScope.set(tracer.withSpan(span));
                }

                return records;
            }

            @Override
            public void success(@NonNull ConsumerRecords<K, V> records,
                                @NonNull Consumer<K, V> consumer) {
                closeSpan();
            }

            @Override
            public void failure(@NonNull ConsumerRecords<K, V> records,
                                @NonNull Exception exception,
                                @NonNull Consumer<K, V> consumer) {
                Span span = currentSpan.get();
                if (span != null) {
                    span.error(exception);
                }

                closeSpan();
            }

            private void closeSpan() {
                Tracer.SpanInScope scope = currentScope.get();
                if (scope != null) {
                    scope.close();
                    currentScope.remove();
                }

                Span span = currentSpan.get();
                if (span != null) {
                    span.end();
                    currentSpan.remove();
                }
            }
        };
    }

    private String resolveKafkaRemoteServiceName() {
        try (AdminClient adminClient = AdminClient.create(consumerConfigs())) {
            String clusterId = adminClient.describeCluster()
                    .clusterId()
                    .get(5, TimeUnit.SECONDS);
            return String.format("%s: %s", APACHE_KAFKA, clusterId.toLowerCase(Locale.ROOT));
        } catch (Exception e) {
            return APACHE_KAFKA;
        }
    }
}