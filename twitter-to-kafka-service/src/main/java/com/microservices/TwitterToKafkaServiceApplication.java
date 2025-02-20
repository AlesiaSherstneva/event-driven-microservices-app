package com.microservices;

import com.microservices.config.TwitterToKafkaConfig;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;

@SpringBootApplication
@AllArgsConstructor
public class TwitterToKafkaServiceApplication implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterToKafkaServiceApplication.class);

    private final TwitterToKafkaConfig twitterToKafkaConfig;

    public static void main(String[] args) {
        SpringApplication.run(TwitterToKafkaServiceApplication.class, args);
    }

    @Override
    public void run(String... args) {
        LOGGER.info("The application is started");
        LOGGER.info(Arrays.toString(
            twitterToKafkaConfig.getTwitterKeywords().toArray(new String[]{})
        ));
    }
}