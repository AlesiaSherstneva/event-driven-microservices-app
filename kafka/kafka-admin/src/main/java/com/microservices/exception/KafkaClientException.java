package com.microservices.exception;

public class KafkaClientException extends RuntimeException {
    public KafkaClientException(String message) {
        super(message);
    }

    public KafkaClientException(String message, Throwable cause) {
        super(message, cause);
    }
}