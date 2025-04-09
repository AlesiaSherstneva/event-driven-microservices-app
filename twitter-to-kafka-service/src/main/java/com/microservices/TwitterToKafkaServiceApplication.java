package com.microservices;

import com.microservices.init.StreamInitializer;
import com.microservices.runner.StreamRunner;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import twitter4j.TwitterException;

@SpringBootApplication
@AllArgsConstructor
public class TwitterToKafkaServiceApplication implements CommandLineRunner {
    private final StreamInitializer streamInitializer;
    private final StreamRunner streamRunner;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterToKafkaServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) throws TwitterException {
        LOGGER.info("The application is started");

        streamInitializer.init();
        streamRunner.start();
    }
}