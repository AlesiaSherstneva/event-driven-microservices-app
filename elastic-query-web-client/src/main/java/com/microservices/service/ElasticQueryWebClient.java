package com.microservices.service;

import com.microservices.common.model.ElasticQueryWebClientRequestModel;
import com.microservices.common.model.ElasticQueryWebClientResponseModel;

import java.util.List;

public interface ElasticQueryWebClient {
    List<ElasticQueryWebClientResponseModel> getDataByText(ElasticQueryWebClientRequestModel requestModel);
}