package com.microservices.dataaccess.repository.impl;

import com.microservices.dataaccess.entity.BaseEntity;
import com.microservices.dataaccess.repository.AnalyticsCustomRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import java.util.Collection;

@Repository
public class AnalyticsRepositoryImpl<T extends BaseEntity<PK>, PK> implements AnalyticsCustomRepository<T, PK> {
    @PersistenceContext
    protected EntityManager entityManager;

    @Value("${spring.jpa.properties.hibernate.jdbc.batch_size}")
    protected int batchSize;

    private static final Logger LOGGER = LoggerFactory.getLogger(AnalyticsRepositoryImpl.class);

    @Override
    public <S extends T> PK persist(S entity) {
        entityManager.persist(entity);

        return entity.getId();
    }

    @Override
    public <S extends T> void batchPersist(Collection<S> entities) {
        if (entities.isEmpty()) {
            LOGGER.info("No entity found to insert");
            return;
        }

        int batchCount = 0;
        for (S entity: entities) {
            LOGGER.trace("Persisting entity with id {}", entity.getId());

            entityManager.persist(entity);
            batchCount++;

            if (batchCount % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }

        if (batchCount % batchSize != 0) {
            entityManager.flush();
            entityManager.clear();
        }
    }

    @Override
    public <S extends T> S merge(S entity) {
        return entityManager.merge(entity);
    }

    @Override
    public <S extends T> void batchMerge(Collection<S> entities) {
        if (entities.isEmpty()) {
            LOGGER.info("No entity found to insert");
            return;
        }

        int batchCount = 0;
        for (S entity: entities) {
            LOGGER.trace("Merging entity with id {}", entity.getId());

            entityManager.merge(entity);
            batchCount++;

            if (batchCount % batchSize == 0) {
                entityManager.flush();
                entityManager.clear();
            }
        }
    }

    @Override
    public void clear() {
        entityManager.clear();
    }
}