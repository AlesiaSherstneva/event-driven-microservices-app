package com.microservices.controller;

import com.microservices.model.AnalyticsDataFallbackModel;
import com.microservices.model.QueryServiceFallbackModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallback")
public class FallbackController {
    private static final Logger LOGGER = LoggerFactory.getLogger(FallbackController.class);

    @PostMapping("/query-fallback")
    public ResponseEntity<QueryServiceFallbackModel> queryServiceFallback() {
        LOGGER.info("Returning fallback result for elastic-query-service!");

        return ResponseEntity.ok(QueryServiceFallbackModel.builder()
                .fallbackMessage("Fallback result for elastic-query-service!")
                .build());
    }

    @PostMapping("/analytics-fallback")
    public ResponseEntity<AnalyticsDataFallbackModel> analyticsServiceFallback() {
        LOGGER.info("Returning fallback result for analytics-service!");

        return ResponseEntity.ok(AnalyticsDataFallbackModel.builder()
                .wordCount(0L)
                .build());
    }

    @PostMapping("/streams-fallback")
    public ResponseEntity<AnalyticsDataFallbackModel> streamsServiceFallback() {
        LOGGER.info("Returning fallback result for kafka-streams-service!");

        return ResponseEntity.ok(AnalyticsDataFallbackModel.builder()
                .wordCount(0L)
                .build());
    }
}