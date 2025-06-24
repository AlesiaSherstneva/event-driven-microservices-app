package com.microservices.exception;

public class ElasticQueryClientException extends RuntimeException {
    public ElasticQueryClientException(String message) {
        super(message);
    }
}