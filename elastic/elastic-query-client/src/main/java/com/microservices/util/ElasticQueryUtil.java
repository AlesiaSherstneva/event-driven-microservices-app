package com.microservices.util;

import com.microservices.elastic.model.IndexModel;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class ElasticQueryUtil<T extends IndexModel> {
    public Query getSearchQueryById(String id) {
        return NativeQuery.builder()
                .withQuery(q -> q.ids(i -> i.values(id)))
                .build();
    }

    public Query getSearchQueryByFieldText(String field, String text) {
        return NativeQuery.builder()
                .withQuery(q -> q.bool(
                        b -> b.must(
                                m -> m.match(
                                        t -> t.field(field).query(text)
                                )
                        )
                ))
                .build();
    }

    public Query getSearchQueryForAll() {
        return NativeQuery.builder()
                .withQuery(q -> q.bool(
                        b -> b.must(
                                m -> m.matchAll(ma -> ma)
                        )
                ))
                .build();
    }
}