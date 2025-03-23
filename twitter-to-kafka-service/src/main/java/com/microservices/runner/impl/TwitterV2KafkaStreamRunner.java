package com.microservices.runner.impl;

import com.microservices.config.TwitterToKafkaConfigData;
import com.microservices.runner.StreamRunner;
import com.microservices.runner.helper.TwitterV2StreamHelper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnExpression("${twitter-to-kafka-service.enable-v2-tweets} && not ${twitter-to-kafka-service.enable-mock-tweets}")
public class TwitterV2KafkaStreamRunner implements StreamRunner {
    private final TwitterToKafkaConfigData config;
    private final TwitterV2StreamHelper helper;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterV2KafkaStreamRunner.class);

    @Override
    public void start() {
        String bearerToken = config.getTwitterV2BearerToken();

        if (bearerToken != null) {
            try {
                helper.setupRules(bearerToken, getRules());
                helper.connectStream(bearerToken);
            } catch (IOException | URISyntaxException ex) {
                String errorMessage = String.format("Error streaming tweets! %s", ex);

                LOGGER.error(errorMessage);
                throw new RuntimeException(errorMessage);
            }
        } else {
            String errorMessage = "There was a problem with your bearer token. " +
                    "Please make sure you set the TWITTER_BEARER_TOKEN environment variable";

            LOGGER.error(errorMessage);
            throw new RuntimeException(errorMessage);
        }
    }

    private Map<String, String> getRules() {
        List<String> keywords = config.getTwitterKeywords();

        Map<String, String> rules = new HashMap<>();
        for (String keyword : keywords) {
            rules.put(keyword, String.format("Keyword: %s", keyword));
        }

        LOGGER.info("Created filter for twitter stream for keywords: {}", keywords);

        return rules;
    }
}