-- :analytics AnalyticsOutboxEntity mapping (Bundle D outbox pattern)
-- Versioned at V100 to leave headroom (V1-V99) for future :analytics base schema
-- additions and to stay well clear of unrelated module migrations.
-- :analytics currently runs with spring.flyway.enabled=false and JPA_DDL_AUTO=update;
-- this file is the canonical reference DDL for when Flyway is enabled here.
-- Multi-instance publisher coordination uses SELECT ... FOR UPDATE SKIP LOCKED,
-- so no extra claimed_at/owner columns are required in this version.
CREATE SEQUENCE IF NOT EXISTS analytics_outbox_seq INCREMENT BY 50 START WITH 1;

CREATE TABLE IF NOT EXISTS analytics_outbox (
    id             BIGINT       PRIMARY KEY,
    aggregate_type VARCHAR(32)  NOT NULL,
    aggregate_id   VARCHAR(128) NOT NULL,
    topic          VARCHAR(64)  NOT NULL,
    payload_json   TEXT         NOT NULL,
    retry_count    INTEGER      NOT NULL,
    created_at     BIGINT       NOT NULL,
    published_at   BIGINT
);

-- Matches @Index(name = "idx_outbox_pending", columnList = "published_at, id")
CREATE INDEX IF NOT EXISTS idx_outbox_pending ON analytics_outbox(published_at, id);
