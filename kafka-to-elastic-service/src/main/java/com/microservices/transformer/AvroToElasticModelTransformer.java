package com.microservices.transformer;

import com.microservices.avro.model.TwitterAvroModel;
import com.microservices.elastic.model.impl.TwitterIndexModel;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Component
public class AvroToElasticModelTransformer {
    public List<TwitterIndexModel> getElasticModels(List<TwitterAvroModel> avroModels) {
        return avroModels.stream()
                .map(am -> TwitterIndexModel.builder()
                        .userId(am.getUserId())
                        .id(String.valueOf(am.getId()))
                        .text(am.getText())
                        .createdAt(LocalDateTime.ofInstant(
                                Instant.ofEpochMilli(am.getCreatedAt()), ZoneId.systemDefault()
                        ))
                        .build())
                .toList();
    }
}