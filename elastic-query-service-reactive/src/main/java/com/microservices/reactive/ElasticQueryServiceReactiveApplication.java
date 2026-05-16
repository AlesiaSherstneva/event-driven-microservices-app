package com.microservices.reactive;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@EnableDiscoveryClient
@SpringBootApplication
@ComponentScan(basePackages = "com.microservices")
public class ElasticQueryServiceReactiveApplication {
    public static void main(String[] args) {
        SpringApplication.run(ElasticQueryServiceReactiveApplication.class, args);
    }
}