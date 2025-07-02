package com.microservices.service.impl;

import com.microservices.config.ElasticConfigData;
import com.microservices.config.ElasticQueryConfigData;
import com.microservices.elastic.model.impl.TwitterIndexModel;
import com.microservices.exception.ElasticQueryClientException;
import com.microservices.service.ElasticQueryClient;
import com.microservices.util.ElasticQueryUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class TwitterElasticQueryClient implements ElasticQueryClient<TwitterIndexModel> {
    private final ElasticConfigData configData;
    private final ElasticQueryConfigData queryConfigData;
    private final ElasticsearchOperations operations;
    private final ElasticQueryUtil<TwitterIndexModel> queryUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterElasticQueryClient.class);

    @Override
    public TwitterIndexModel getIndexModelById(String id) {
        try {
            Query query = queryUtil.getSearchQueryById(id);
            SearchHit<TwitterIndexModel> searchResult =
                    operations.searchOne(query, TwitterIndexModel.class, IndexCoordinates.of(configData.getIndexName()));

            LOGGER.info("Document with id {} retrieved successfully", searchResult.getId());

            return searchResult.getContent();
        } catch (NoSuchElementException ex) {
            String errorMessage = String.format("No document with id %s found at the database", id);
            LOGGER.error(errorMessage);

            throw new ElasticQueryClientException(errorMessage);
        }
    }

    @Override
    public List<TwitterIndexModel> getIndexModelByText(String text) {
        Query query = queryUtil.getSearchQueryByFieldText(queryConfigData.getTextField(), text);
        SearchHits<TwitterIndexModel> searchResult =
                operations.search(query, TwitterIndexModel.class, IndexCoordinates.of(configData.getIndexName()));

        LOGGER.info("{} of documents with text {} retrieved successfully", searchResult.getTotalHits(), text);

        return searchResult.get()
                .map(SearchHit::getContent)
                .toList();
    }

    @Override
    public List<TwitterIndexModel> getAllIndexModels() {
        Query query = queryUtil.getSearchQueryForAll();
        SearchHits<TwitterIndexModel> searchResult =
                operations.search(query, TwitterIndexModel.class, IndexCoordinates.of(configData.getIndexName()));

        LOGGER.info("{} number of documents retrieved successfully", searchResult.getTotalHits());

        return searchResult.get()
                .map(SearchHit::getContent)
                .toList();
    }
}