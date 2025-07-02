package com.microservices.service.impl;

import com.microservices.elastic.model.impl.TwitterIndexModel;
import com.microservices.exception.ElasticQueryClientException;
import com.microservices.repository.TwitterElasticsearchQueryRepository;
import com.microservices.service.ElasticQueryClient;
import com.microservices.util.CollectionsUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Primary
@RequiredArgsConstructor
public class TwitterElasticRepositoryQueryClient implements ElasticQueryClient<TwitterIndexModel> {
    private final TwitterElasticsearchQueryRepository queryRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterElasticRepositoryQueryClient.class);

    @Override
    public TwitterIndexModel getIndexModelById(String id) {
        Optional<TwitterIndexModel> searchResult = queryRepository.findById(id);

        LOGGER.info("Document with id {} retrieved successfully", searchResult.orElseThrow(() ->
                new ElasticQueryClientException(String.format("No document with id %s found at the database", id))).getId());

        return searchResult.get();
    }

    @Override
    public List<TwitterIndexModel> getIndexModelByText(String text) {
        List<TwitterIndexModel> searchResult = queryRepository.findByText(text);

        LOGGER.info("{} of documents with text {} retrieved successfully", searchResult.size(), text);

        return searchResult;
    }

    @Override
    public List<TwitterIndexModel> getAllIndexModels() {
        List<TwitterIndexModel> searchResult = CollectionsUtil
                .getInstance()
                .getListFromIterable(queryRepository.findAll());

        LOGGER.info("{} number of documents retrieved successfully", searchResult.size());

        return searchResult;
    }
}