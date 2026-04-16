package com.microservices.dataaccess.repository;

import com.microservices.dataaccess.entity.AnalyticsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface AnalyticsRepository extends JpaRepository<AnalyticsEntity, UUID>,
        AnalyticsCustomRepository<AnalyticsEntity, UUID> {
    @Query("SELECT e FROM AnalyticsEntity e " +
            "WHERE e.word = :word ORDER BY e.recordDate DESC")
    List<AnalyticsEntity> getAnalyticsEntitiesByWord(@Param("word") String word, Pageable pageable);
}