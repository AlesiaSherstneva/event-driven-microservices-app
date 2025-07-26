package com.microservices.common.exception;

public class ElasticQueryWebClientException extends RuntimeException {
    public ElasticQueryWebClientException(String message) {
        super(message);
    }
}