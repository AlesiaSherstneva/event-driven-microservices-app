package com.microservices.runner.impl;

import com.microservices.config.TwitterToKafkaConfigData;
import com.microservices.listener.TwitterKafkaStatusListener;
import com.microservices.runner.StreamRunner;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@ConditionalOnExpression("not ${twitter-to-kafka-service.enable-mock-tweets} && not ${twitter-to-kafka-service.enable-v2-tweets}")
public class TwitterKafkaStreamRunner implements StreamRunner {
    private final TwitterToKafkaConfigData config;
    private final TwitterKafkaStatusListener listener;

    private TwitterStream twitterStream;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterKafkaStreamRunner.class);

    @Override
    public void start() {
        // it doesn't work with free X (ex-Twitter) account
        twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);

        String[] keywords = config.getTwitterKeywords().toArray(new String[0]);
        FilterQuery filterQuery = new FilterQuery(keywords);
        twitterStream.filter(filterQuery);

        LOGGER.info("Started filtering twitter stream for keywords: {}", Arrays.toString(keywords));
    }

    @PreDestroy
    public void shutdown() {
        if (twitterStream != null) {
            LOGGER.info("Closing twitter stream!");

            twitterStream.shutdown();
        }
    }
}