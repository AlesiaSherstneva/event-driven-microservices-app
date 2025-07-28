package com.microservices.reactive.service.impl;

import com.microservices.common.model.ElasticQueryServiceResponseModel;
import com.microservices.common.transformer.ElasticToResponseModelTransformer;
import com.microservices.elastic.model.impl.TwitterIndexModel;
import com.microservices.reactive.service.ElasticQueryClientReactive;
import com.microservices.reactive.service.ElasticQueryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service
@RequiredArgsConstructor
public class TwitterElasticQueryService implements ElasticQueryService {
    private final ElasticQueryClientReactive<TwitterIndexModel> queryClient;
    private final ElasticToResponseModelTransformer transformer;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterElasticQueryService.class);

    @Override
    public Flux<ElasticQueryServiceResponseModel> getDocumentByText(String text) {
        LOGGER.info("Querying reactive elasticsearch for text: {}", text);

        return queryClient.getIndexModelByText(text)
                .map(transformer::getResponseModel);
    }
}