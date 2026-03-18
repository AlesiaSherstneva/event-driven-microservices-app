CREATE SCHEMA IF NOT EXISTS analytics;

CREATE TABLE IF NOT EXISTS analytics.twitter_analytics
(
    id          UUID    NOT NULL,
    word        VARCHAR NOT NULL,
    word_count  BIGINT  NOT NULL,
    record_date TIMESTAMP,
    CONSTRAINT twitter_analytics_pkey PRIMARY KEY (id)
);

ALTER TABLE analytics.twitter_analytics
    OWNER TO postgres;

CREATE INDEX idx_word_by_date
    ON analytics.twitter_analytics (word ASC NULLS LAST, record_date DESC NULLS LAST);