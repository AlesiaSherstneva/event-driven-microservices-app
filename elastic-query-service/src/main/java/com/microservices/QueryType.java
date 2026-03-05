package com.microservices;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QueryType {
    KAFKA_STATE_STORE("KAFKA_STATE_STORE"),
    ANALYTICS_DATABASE("ANALYTICS_DATABASE");

    private final String type;
}