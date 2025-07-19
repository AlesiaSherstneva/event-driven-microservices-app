package com.microservices.service;

import com.microservices.model.ElasticQueryWebClientRequestModel;
import com.microservices.model.ElasticQueryWebClientResponseModel;

import java.util.List;

public interface ElasticQueryWebClient {
    List<ElasticQueryWebClientResponseModel> getDataByText(ElasticQueryWebClientRequestModel requestModel);
}