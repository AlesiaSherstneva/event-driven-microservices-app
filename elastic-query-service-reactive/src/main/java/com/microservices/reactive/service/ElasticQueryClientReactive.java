package com.microservices.reactive.service;

import com.microservices.elastic.model.IndexModel;
import com.microservices.elastic.model.impl.TwitterIndexModel;
import reactor.core.publisher.Flux;

public interface ElasticQueryClientReactive<T extends IndexModel> {
    Flux<TwitterIndexModel> getIndexModelByText(String text);
}