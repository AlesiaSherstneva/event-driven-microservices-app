package com.microservices.model.assembler;

import com.microservices.api.ElasticDocumentController;
import com.microservices.elastic.model.impl.TwitterIndexModel;
import com.microservices.model.ElasticQueryServiceResponseModel;
import com.microservices.transformer.ElasticToResponseModelTransformer;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class ElasticQueryServiceResponseModelAssembler
        extends RepresentationModelAssemblerSupport<TwitterIndexModel, ElasticQueryServiceResponseModel> {
    private final ElasticToResponseModelTransformer transformer;

    public ElasticQueryServiceResponseModelAssembler(ElasticToResponseModelTransformer transformer) {
        super(ElasticDocumentController.class, ElasticQueryServiceResponseModel.class);
        this.transformer = transformer;
    }

    @NonNull
    @Override
    public ElasticQueryServiceResponseModel toModel(@NonNull TwitterIndexModel indexModel) {
        ElasticQueryServiceResponseModel responseModel = transformer.getResponseModel(indexModel);
        responseModel.add(
                linkTo(methodOn(ElasticDocumentController.class)
                        .getDocumentById((indexModel.getId())))
                        .withSelfRel(),
                linkTo(ElasticDocumentController.class)
                        .withRel("documents")
        );

        return responseModel;
    }

    public List<ElasticQueryServiceResponseModel> toModels(List<TwitterIndexModel> indexModels) {
        return indexModels.stream()
                .map(this::toModel)
                .toList();
    }
}