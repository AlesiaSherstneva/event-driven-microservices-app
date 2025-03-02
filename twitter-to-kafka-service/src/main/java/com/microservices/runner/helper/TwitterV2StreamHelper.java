package com.microservices.runner.helper;

import com.microservices.config.TwitterToKafkaConfig;
import com.microservices.listener.TwitterKafkaStatusListener;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TwitterV2StreamHelper {
    private final TwitterToKafkaConfig config;
    private final TwitterKafkaStatusListener listener;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterV2StreamHelper.class);
    private static final String TWEET_AS_RAW_JSON = """
            {
                "created_at": {0},
                "id": {1},
                "text": {2},
                "user": {
                    "id": {3}
                }
            }
            """;
    private static final String TWITTER_STATUS_DATE_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";

    void connectStream(String bearerToken) {

    }
}