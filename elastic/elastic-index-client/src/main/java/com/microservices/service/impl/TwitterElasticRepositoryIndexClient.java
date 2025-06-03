package com.microservices.service.impl;

import com.microservices.elastic.model.impl.TwitterIndexModel;
import com.microservices.repository.TwitterElasticsearchIndexRepository;
import com.microservices.service.ElasticIndexClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "elastic-config.is-repository", havingValue = "true", matchIfMissing = true)
public class TwitterElasticRepositoryIndexClient implements ElasticIndexClient<TwitterIndexModel> {
    private final TwitterElasticsearchIndexRepository indexRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterElasticRepositoryIndexClient.class);

    @Override
    public List<String> save(List<TwitterIndexModel> documents) {
        List<TwitterIndexModel> repoResponse = (List<TwitterIndexModel>) indexRepository.saveAll(documents);
        List<String> documentIds = repoResponse.stream()
                .map(TwitterIndexModel::getId)
                .toList();

        LOGGER.info("Documents indexed successfully with type: {} and ids: {}",
                TwitterIndexModel.class.getName(), documentIds);

        return documentIds;
    }
}