package com.microservices.runner.impl;

import com.microservices.config.TwitterToKafkaConfig;
import com.microservices.exception.TwitterToKafkaException;
import com.microservices.listener.TwitterKafkaStatusListener;
import com.microservices.runner.StreamRunner;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class MockKafkaStreamRunner implements StreamRunner {
    private final TwitterToKafkaConfig config;
    private final TwitterKafkaStatusListener listener;

    private static final Logger LOGGER = LoggerFactory.getLogger(MockKafkaStreamRunner.class);
    private static final Random RANDOM = new Random();
    private static final String[] WORDS = new String[]{
            "Lorem", "ipsum", "dolor", "sit", "amet", "consectetuer", "adipiscing", "elit",
            "Maecenas", "porttitor", "conque", "massa", "Fusce", "posuere", "magna", "sed",
            "pulvinar", "ultricies", "purus", "lectus", "malesuada", "libero"
    };
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

    @Override
    public void start() {
        String[] keywords = config.getTwitterKeywords().toArray(new String[0]);
        int minTweetLength = config.getMockMinTweetLength();
        int maxTweetLength = config.getMockMaxTweetLength();
        long sleepTimeMs = config.getMockSleepMs();

        LOGGER.info("Starting mock filtering twitter streams for keywords: {}", Arrays.toString(keywords));

        simulateTwitterStream(keywords, minTweetLength, maxTweetLength, sleepTimeMs);
    }

    private void simulateTwitterStream(String[] keywords, int minTweetLength, int maxTweetLength, long sleepTimeMs) {
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                while (true) {
                    String formattedTweetAsRawJson = getFormattedTweet(keywords, minTweetLength, maxTweetLength);
                    Status status = TwitterObjectFactory.createStatus(formattedTweetAsRawJson);
                    listener.onStatus(status);
                    sleep(sleepTimeMs);
                }
            } catch (TwitterException ex) {
                LOGGER.error("Error creating twitter status!", ex);
            }
        });
    }

    private void sleep(long sleepTimeMs) {
        try {
            Thread.sleep(sleepTimeMs);
        } catch (InterruptedException ex) {
            throw new TwitterToKafkaException("Error while sleeping for waiting new status to create!");
        }
    }

    private String getFormattedTweet(String[] keywords, int minTweetLength, int maxTweetLength) {
        String[] params = new String[]{
                ZonedDateTime.now().format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT)),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE)),
                gerRandomTweetContent(keywords, minTweetLength, maxTweetLength),
                String.valueOf(ThreadLocalRandom.current().nextLong(Long.MAX_VALUE))
        };

        String tweet = TWEET_AS_RAW_JSON;

        for (int i = 0; i < params.length; i++) {
            tweet = tweet.replace("{" + i + "}", params[i]);
        }

        return tweet;
    }

    private String gerRandomTweetContent(String[] keywords, int minTweetLength, int maxTweetLength) {
        StringBuilder tweet = new StringBuilder();
        int tweetLength = RANDOM.nextInt(maxTweetLength - minTweetLength + 1) + minTweetLength;

        for (int i = 0; i < tweetLength; i++) {
            tweet.append(WORDS[RANDOM.nextInt(WORDS.length)]).append(" ");
            if (i == tweetLength / 2) {
                tweet.append(keywords[RANDOM.nextInt(keywords.length)]).append(" ");
            }
        }

        return tweet.toString().trim();
    }
}