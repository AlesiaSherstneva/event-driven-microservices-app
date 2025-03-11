package com.microservices.exception;

public class TwitterToKafkaException extends RuntimeException {
    public TwitterToKafkaException(String message) {
        super(message);
    }
}