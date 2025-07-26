package com.microservices.service.impl;

import com.microservices.common.exception.ElasticQueryWebClientException;
import com.microservices.common.model.ElasticQueryWebClientRequestModel;
import com.microservices.common.model.ElasticQueryWebClientResponseModel;
import com.microservices.config.ElasticQueryWebClientConfigData;
import com.microservices.service.ElasticQueryWebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class TwitterElasticQueryWebClient implements ElasticQueryWebClient {
    private final WebClient.Builder webClientBuilder;
    private final ElasticQueryWebClientConfigData clientConfigData;

    public TwitterElasticQueryWebClient(@Qualifier("webClientBuilder") WebClient.Builder webClientBuilder,
                                        ElasticQueryWebClientConfigData clientConfigData) {
        this.webClientBuilder = webClientBuilder;
        this.clientConfigData = clientConfigData;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterElasticQueryWebClient.class);

    @Override
    public List<ElasticQueryWebClientResponseModel> getDataByText(ElasticQueryWebClientRequestModel requestModel) {
        LOGGER.info("Querying by text: {}", requestModel.getText());

        return getWebClient(requestModel)
                .bodyToFlux(ElasticQueryWebClientResponseModel.class)
                .collectList()
                .block();
    }

    private WebClient.ResponseSpec getWebClient(ElasticQueryWebClientRequestModel requestModel) {
        return webClientBuilder.build()
                .method(HttpMethod.valueOf(clientConfigData.getQueryByText().getMethod()))
                .uri(clientConfigData.getQueryByText().getUri())
                .accept(MediaType.valueOf(clientConfigData.getQueryByText().getAccept()))
                .body(BodyInserters.fromPublisher(Mono.just(requestModel), createParametrizedTypeReference()))
                .retrieve()
                .onStatus(
                        httpStatus -> httpStatus.equals(HttpStatus.UNAUTHORIZED),
                        clientResponse -> Mono.error(new BadCredentialsException("Not authenticated!"))
                )
                .onStatus(
                        HttpStatusCode::is4xxClientError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("Client error!")
                                .flatMap(body -> Mono.error(new ElasticQueryWebClientException(body)))
                )
                .onStatus(
                        HttpStatusCode::is5xxServerError,
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .defaultIfEmpty("Server error!")
                                .flatMap(body -> Mono.error(new ElasticQueryWebClientException(body)))
                );
    }

    private <T> ParameterizedTypeReference<T> createParametrizedTypeReference() {
        return new ParameterizedTypeReference<>() {
        };
    }
}