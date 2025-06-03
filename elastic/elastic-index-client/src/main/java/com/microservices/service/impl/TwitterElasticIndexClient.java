package com.microservices.service.impl;

import com.microservices.config.ElasticConfigData;
import com.microservices.elastic.model.impl.TwitterIndexModel;
import com.microservices.service.ElasticIndexClient;
import com.microservices.util.ElasticIndexUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elastic-config.is-repository", havingValue = "false")
public class TwitterElasticIndexClient implements ElasticIndexClient<TwitterIndexModel> {
    private final ElasticConfigData configData;
    private final ElasticsearchOperations operations;
    private final ElasticIndexUtil<TwitterIndexModel> indexUtil;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterElasticIndexClient.class);

    @Override
    public List<String> save(List<TwitterIndexModel> documents) {
        List<IndexQuery> indexQueries = indexUtil.getIndexQueries(documents);
        List<String> documentIds = indexQueries.stream()
                .map(query -> operations.index(query, IndexCoordinates.of(configData.getIndexName())))
                .toList();

        LOGGER.info("Documents indexed successfully with type: {} and ids: {}",
                TwitterIndexModel.class.getName(), documentIds);

        return documentIds;
    }
}