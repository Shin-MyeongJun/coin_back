-- :alert AlertRuleEntity mapping
-- Renumbered from V1 to V10 to avoid collision with :user V1__account / V2__api_key
-- on the shared :api Flyway schema history.
CREATE TABLE IF NOT EXISTS alert_rule (
    id           BIGSERIAL       PRIMARY KEY,
    user_id      VARCHAR(64)     NOT NULL,
    label        VARCHAR(50)     NOT NULL,
    target_type  VARCHAR(16)     NOT NULL,
    asset_symbol VARCHAR(16)     NOT NULL,
    operator     VARCHAR(4)      NOT NULL,
    threshold    NUMERIC(20, 8)  NOT NULL,
    cooldown_sec INTEGER         NOT NULL,
    channels     VARCHAR(64)     NOT NULL,
    active       BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at   BIGINT          NOT NULL,
    updated_at   BIGINT          NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_alert_rule_user ON alert_rule(user_id, active);
