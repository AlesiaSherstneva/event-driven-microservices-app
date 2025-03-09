package com.microservices.runner.helper;

import com.microservices.config.TwitterToKafkaConfig;
import com.microservices.listener.TwitterKafkaStatusListener;
import lombok.RequiredArgsConstructor;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.net.URIBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
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
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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

    public void connectStream(String bearerToken) throws IOException, URISyntaxException {
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
                        if (!nextLine.isEmpty()) {
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

    public void setupRules(String bearerToken, Map<String, String> rules) throws IOException, URISyntaxException {
        List<String> existingRules = getRules(bearerToken);

        if (existingRules.size() > 0) {
            deleteRules(bearerToken, existingRules);
        }

        createRules(bearerToken, rules);

        LOGGER.info("Created rules for twitter stream: {}", rules.keySet().toArray());
    }

    private void createRules(String bearerToken, Map<String, String> rules) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(StandardCookieSpec.RELAXED).build())
                .build()) {
            URIBuilder uri = new URIBuilder(config.getTwitterV2RulesBaseUrl());

            HttpPost httpPost = new HttpPost(uri.build());
            httpPost.setHeader("Authorization", String.format("Bearer %s", bearerToken));
            httpPost.setHeader("Content-type", "application/json");

            StringEntity body = new StringEntity(getFormattedStringForCreatingRules(rules));
            httpPost.setEntity(body);

            HttpClientResponseHandler<Void> responseHandler = (ClassicHttpResponse response) -> {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    System.out.println(EntityUtils.toString(entity, "UTF-8"));
                }
                return null;
            };

            httpClient.execute(httpPost, HttpClientContext.create(), responseHandler);
        }
    }

    private String getFormattedStringForCreatingRules(Map<String, String> rules) {
        StringBuilder formattedString = new StringBuilder("{\"add\": [%s]}");

        if (rules.size() == 1) {
            String key = rules.keySet().iterator().next();
            formattedString.append("{\"value\": \"")
                    .append(key)
                    .append("\", \"tag\": \"")
                    .append(rules.get(key))
                    .append("\"}");
            return formattedString.toString();
        } else {
            for (Map.Entry<String, String> entry : rules.entrySet()) {
                String value = entry.getKey();
                String tag = entry.getValue();

                formattedString.append("{\"value\": \"")
                        .append(value)
                        .append("\", \"tag\": \"")
                        .append(tag)
                        .append("\"},");
            }
            return formattedString.substring(0, formattedString.length() - 1);
        }
    }

    private List<String> getRules(String bearerToken) throws IOException, URISyntaxException {
        List<String> rules = new ArrayList<>();

        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(StandardCookieSpec.RELAXED).build())
                .build()) {
            URIBuilder uri = new URIBuilder(config.getTwitterV2RulesBaseUrl());

            HttpGet httpGet = new HttpGet(uri.build());
            httpGet.setHeader("Authorization", String.format("Bearer %s", bearerToken));
            httpGet.setHeader("Content-type", "application/json");

            HttpClientResponseHandler<Void> responseHandler = (ClassicHttpResponse response) -> {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    JSONObject json = new JSONObject(EntityUtils.toString(entity, "UTF-8"));
                    if (json.length() > 1 && json.has("data")) {
                        JSONArray jsonArray = (JSONArray) json.get("data");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = (JSONObject) jsonArray.get(i);
                            rules.add(jsonObject.getString("id"));
                        }
                    }
                }
                return null;
            };

            httpClient.execute(httpGet, HttpClientContext.create(), responseHandler);
        }

        return rules;
    }

    private void deleteRules(String bearerToken, List<String> rules) throws IOException, URISyntaxException {
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultRequestConfig(RequestConfig.custom()
                        .setCookieSpec(StandardCookieSpec.RELAXED).build())
                .build()) {
            URIBuilder uri = new URIBuilder(config.getTwitterV2RulesBaseUrl());

            HttpPost httpPost = new HttpPost(uri.build());
            httpPost.setHeader("Authorization", String.format("Bearer %s", bearerToken));
            httpPost.setHeader("Content-type", "application/json");

            StringEntity body = new StringEntity(formatStringForDeletingRules(rules));
            httpPost.setEntity(body);

            HttpClientResponseHandler<Void> responseHandler = (ClassicHttpResponse response) -> {
                final HttpEntity entity = response.getEntity();
                if (entity != null) {
                    System.out.println(EntityUtils.toString(entity, "UTF-8"));
                }
                return null;
            };

            httpClient.execute(httpPost, HttpClientContext.create(), responseHandler);
        }
    }

    private String formatStringForDeletingRules(List<String> ids) {
        StringBuilder formattedString = new StringBuilder("{ \"delete\": { \"ids\": [%s]}}");

        if (ids.size() == 1) {
            formattedString.append("\"")
                    .append(ids.get(0))
                    .append("\"");
            return formattedString.toString();
        } else {
            for (String id : ids) {
                formattedString.append("\"")
                        .append(id)
                        .append("\",");
            }
            return formattedString.substring(0, formattedString.length() - 1);
        }
    }

    private String getFormattedTweet(String data) {
        JSONObject jsonData = (JSONObject) new JSONObject(data).get("data");

        String[] params = new String[]{
                ZonedDateTime.parse(jsonData.get("created_at").toString()).withZoneSameInstant(ZoneId.of("UTC"))
                        .format(DateTimeFormatter.ofPattern(TWITTER_STATUS_DATE_FORMAT, Locale.ENGLISH)),
                jsonData.get("id").toString(),
                jsonData.get("text").toString().replaceAll("\"", "\\\\\""),
                jsonData.get("author_id").toString()
        };

        return formatTweetAsJsonWithParams(params);
    }

    private String formatTweetAsJsonWithParams(String[] params) {
        String tweet = TWEET_AS_RAW_JSON;

        for (int i = 0; i < params.length; i++) {
            tweet = tweet.replace("{" + i + "}", params[i]);
        }

        return tweet;
    }
}