package com.microservices.service.impl;

import com.microservices.QueryType;
import com.microservices.common.model.ElasticQueryServiceResponseModel;
import com.microservices.config.ElasticQueryServiceConfigData;
import com.microservices.elastic.model.impl.TwitterIndexModel;
import com.microservices.exception.ElasticQueryServiceException;
import com.microservices.model.ElasticQueryServiceAnalyticsResponseModel;
import com.microservices.model.ElasticQueryServiceWordCountResponseModel;
import com.microservices.model.assembler.ElasticQueryServiceResponseModelAssembler;
import com.microservices.service.ElasticQueryClient;
import com.microservices.service.ElasticQueryService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TwitterElasticQueryService implements ElasticQueryService {
    private final ElasticQueryServiceResponseModelAssembler assembler;
    private final ElasticQueryClient<TwitterIndexModel> queryClient;
    private final ElasticQueryServiceConfigData configData;
    private final WebClient.Builder webClientBuilder;

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterElasticQueryService.class);

    @Override
    public ElasticQueryServiceResponseModel getDocumentById(String id) {
        LOGGER.info("Querying elasticsearch by id: {}", id);

        return assembler.toModel(queryClient.getIndexModelById(id));
    }

    @Override
    public ElasticQueryServiceAnalyticsResponseModel getDocumentByText(String text, String accessToken) {
        LOGGER.info("Querying elasticsearch by text: {}", text);

        List<ElasticQueryServiceResponseModel> queryResponseModels =
                assembler.toModels(queryClient.getIndexModelByText(text));

        return ElasticQueryServiceAnalyticsResponseModel.builder()
                .queryResponseModels(queryResponseModels)
                .wordCount(getWordCount(text, accessToken))
                .build();
    }

    private Long getWordCount(String text, String accessToken) {
        if (QueryType.KAFKA_STATE_STORE.getType().equals(configData.getWebClient().getQueryType())) {
            return getFromKafkaStateStore(text, accessToken).getWordCount();
        }

        return 0L;
    }

    private ElasticQueryServiceWordCountResponseModel getFromKafkaStateStore(String text, String accessToken) {
        ElasticQueryServiceConfigData.Query queryFromKafkaStateStore =
                configData.getQueryFromKafkaStateStore();

        return retrieveResponseModel(text, accessToken, queryFromKafkaStateStore);
    }

    private ElasticQueryServiceWordCountResponseModel retrieveResponseModel(String text,
                                                                            String accessToken,
                                                                            ElasticQueryServiceConfigData.Query queryFromKafkaStateStore) {
        return webClientBuilder.build()
                .method(HttpMethod.valueOf(queryFromKafkaStateStore.getMethod()))
                .uri(queryFromKafkaStateStore.getUri(), uriBuilder -> uriBuilder.build(text))
                .headers(h -> h.setBearerAuth(accessToken))
                .accept(MediaType.valueOf(queryFromKafkaStateStore.getAccept()))
                .retrieve()
                .onStatus(
                        s -> s.equals(HttpStatus.UNAUTHORIZED),
                        clientResponse -> Mono.just(new BadCredentialsException("Not authenticated"))
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> Mono.just(new ElasticQueryServiceException(
                                "Client error: %s".formatted(clientResponse.statusCode().value())
                        ))
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        clientResponse -> Mono.just(new Exception(
                                "Server error: %s".formatted(clientResponse.statusCode().value())
                        ))
                )
                .bodyToMono(ElasticQueryServiceWordCountResponseModel.class)
                .log()
                .block();
    }

    @Override
    public List<ElasticQueryServiceResponseModel> getAllDocuments() {
        LOGGER.info("Querying all documents in elasticsearch");

        return assembler.toModels(queryClient.getAllIndexModels());
    }
}