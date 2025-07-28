package com.microservices.reactive.api;

import com.microservices.common.model.ElasticQueryServiceRequestModel;
import com.microservices.common.model.ElasticQueryServiceResponseModel;
import com.microservices.reactive.service.ElasticQueryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/documents")
@RequiredArgsConstructor
public class ElasticDocumentController {
    private final ElasticQueryService queryService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticDocumentController.class);

    @PostMapping(value = "/by-text",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public Flux<ElasticQueryServiceResponseModel> getDocumentByText(
            @RequestBody @Valid ElasticQueryServiceRequestModel requestModel) {
        Flux<ElasticQueryServiceResponseModel> response = queryService.getDocumentByText(requestModel.getText());
        response = response.log();

        LOGGER.info("Returning from query reactive service for text: {}", requestModel.getText());

        return response;
    }
}