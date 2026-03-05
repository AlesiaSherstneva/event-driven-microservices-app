package com.microservices.exception;

public class ElasticQueryServiceException extends RuntimeException {
    public ElasticQueryServiceException(String message) {
        super(message);
    }
}