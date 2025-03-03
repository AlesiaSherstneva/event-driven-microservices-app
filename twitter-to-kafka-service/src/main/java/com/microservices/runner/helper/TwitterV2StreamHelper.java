package com.microservices.runner.helper;

import com.microservices.config.TwitterToKafkaConfig;
import com.microservices.listener.TwitterKafkaStatusListener;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.net.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

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

    void connectStream(String bearerToken) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(StandardCookieSpec.RELAXED).build())
                .build()) {
            URIBuilder uri = new URIBuilder(config.getTwitterV2BaseUrl());

            HttpGet httpGet = new HttpGet(uri.build());
            httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));

            HttpClientResponseHandler<Void> responseHandler = (ClassicHttpResponse response) -> {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(entity.getContent()));
                    String nextLine = reader.readLine();

                    while (nextLine != null) {
                        nextLine = reader.readLine();
                        if(!nextLine.isEmpty()) {
                            String tweet = getFormattedTweet(nextLine);

                            Status status = null;
                            try {
                                status = TwitterObjectFactory.createStatus(tweet);
                            } catch (TwitterException ex) {
                                LOGGER.error("Could not create status for text: {}", tweet, ex);
                            }

                            if (status != null) {
                                listener.onStatus(status);
                            }
                        }
                    }
                }
                return null;
            };

            httpClient.execute(httpGet, HttpClientContext.create(), responseHandler);
        }
    }

    void setupRules(String bearerToken, Map<String, String> rules) {
        List<String> existingRules = getRules(bearerToken);

        if (existingRules.size() > 0) {
            deleteRules(bearerToken, existingRules);
        }

        createRules(bearerToken, rules);

        LOGGER.info("Created rules for twitter stream: {}", rules.keySet().toArray());
    }

    private String getFormattedTweet(String nextLine) {
        return null;
    }
}