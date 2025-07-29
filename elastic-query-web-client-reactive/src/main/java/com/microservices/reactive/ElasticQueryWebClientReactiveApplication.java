package com.microservices.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = "com.microservices")
public class ElasticQueryWebClientReactiveApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElasticQueryWebClientReactiveApplication.class, args);
    }
}