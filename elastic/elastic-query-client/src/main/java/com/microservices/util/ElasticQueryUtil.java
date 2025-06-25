package com.microservices.util;

import com.microservices.elastic.model.IndexModel;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class ElasticQueryUtil<T extends IndexModel> {
    public Query getSearchQueryById(String id) {

    }
}