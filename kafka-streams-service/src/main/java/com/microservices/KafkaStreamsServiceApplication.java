package com.microservices;

import com.microservices.init.StreamsInitializer;
import com.microservices.runner.StreamsRunner;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@AllArgsConstructor
@SpringBootApplication
public class KafkaStreamsServiceApplication implements CommandLineRunner {
    private final StreamsInitializer streamInitializer;
    private final StreamsRunner<String, Long> streamRunner;

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(KafkaStreamsServiceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        LOGGER.info("The application starts...");

        streamInitializer.init();
        streamRunner.start();
    }
}