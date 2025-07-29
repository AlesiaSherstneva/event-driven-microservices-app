package com.microservices.reactive.service;

import com.microservices.common.model.ElasticQueryWebClientRequestModel;
import com.microservices.common.model.ElasticQueryWebClientResponseModel;
import reactor.core.publisher.Flux;

public interface ElasticQueryWebClient {
    Flux<ElasticQueryWebClientResponseModel> getDataByText(ElasticQueryWebClientRequestModel requestModel);
}