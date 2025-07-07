package com.microservices.api;

import com.microservices.model.ElasticQueryServiceRequestModel;
import com.microservices.model.ElasticQueryServiceResponseModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class ElasticDocumentController {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticDocumentController.class);

    @GetMapping
    public ResponseEntity<List<ElasticQueryServiceResponseModel>> getAllDocuments() {
        List<ElasticQueryServiceResponseModel> response = new ArrayList<>();

        LOGGER.info("Elasticsearch returned {} of documents", response.size());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ElasticQueryServiceResponseModel> getDocumentById(@PathVariable String id) {
        ElasticQueryServiceResponseModel response =
                ElasticQueryServiceResponseModel.builder()
                        .id(id)
                        .build();

        LOGGER.info("Elasticsearch returned document with id {}", id);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/by-text")
    public ResponseEntity<List<ElasticQueryServiceResponseModel>> getDocumentByText(
            @RequestBody ElasticQueryServiceRequestModel requestModel) {
        List<ElasticQueryServiceResponseModel> response = new ArrayList<>();

        ElasticQueryServiceResponseModel responseModel =
                ElasticQueryServiceResponseModel.builder()
                        .text(requestModel.getText())
                        .build();
        response.add(responseModel);

        LOGGER.info("Elasticsearch returned {} of documents", response.size());

        return ResponseEntity.ok(response);
    }
}