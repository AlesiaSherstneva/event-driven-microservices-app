package com.microservices.service.impl;

import com.microservices.elastic.model.impl.TwitterIndexModel;
import com.microservices.model.ElasticQueryServiceResponseModel;
import com.microservices.model.assembler.ElasticQueryServiceResponseModelAssembler;
import com.microservices.service.ElasticQueryClient;
import com.microservices.service.ElasticQueryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TwitterElasticQueryService implements ElasticQueryService {
    private final ElasticQueryServiceResponseModelAssembler assembler;
    private final ElasticQueryClient<TwitterIndexModel> queryClient;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterElasticQueryService.class);

    @Override
    public ElasticQueryServiceResponseModel getDocumentById(String id) {
        LOGGER.info("Querying elasticsearch by id: {}", id);

        return assembler.toModel(queryClient.getIndexModelById(id));
    }

    @Override
    public List<ElasticQueryServiceResponseModel> getDocumentByText(String text) {
        LOGGER.info("Querying elasticsearch by text: {}", text);

        return assembler.toModels(queryClient.getIndexModelByText(text));
    }

    @Override
    public List<ElasticQueryServiceResponseModel> getAllDocuments() {
        LOGGER.info("Querying all documents in elasticsearch");

        return assembler.toModels(queryClient.getAllIndexModels());
    }
}