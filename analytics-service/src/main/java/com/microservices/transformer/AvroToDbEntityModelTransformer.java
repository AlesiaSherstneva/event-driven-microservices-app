package com.microservices.transformer;

import com.microservices.avro.model.TwitterAnalyticsAvroModel;
import com.microservices.dataaccess.entity.AnalyticsEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.IdGenerator;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AvroToDbEntityModelTransformer {
    private final IdGenerator idGenerator;

    public List<AnalyticsEntity> getEntityModels(List<TwitterAnalyticsAvroModel> avroModels) {
        return avroModels.stream()
                .map(avroModel -> AnalyticsEntity.builder()
                        .id(idGenerator.generateId())
                        .word(avroModel.getWord())
                        .wordCount(avroModel.getWordCount())
                        .recordDate(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(avroModel.getCreatedAt()),
                                ZoneOffset.UTC))
                        .build())
                .collect(Collectors.toList());
    }
}