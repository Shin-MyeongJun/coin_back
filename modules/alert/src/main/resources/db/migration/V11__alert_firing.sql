-- :alert AlertFiringEntity mapping
-- Renumbered from V2 to V11 to avoid collision with :user V2__api_key
-- on the shared :api Flyway schema history.
CREATE TABLE IF NOT EXISTS alert_firing (
    id             BIGSERIAL       PRIMARY KEY,
    rule_id        BIGINT          NOT NULL,
    user_id        VARCHAR(64)     NOT NULL,
    rule_label     VARCHAR(50)     NOT NULL,
    condition_text VARCHAR(64)     NOT NULL,
    observed_value NUMERIC(20, 8)  NOT NULL,
    fired_at       BIGINT          NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_alert_firing_user_time ON alert_firing(user_id, fired_at DESC);
CREATE INDEX IF NOT EXISTS idx_alert_firing_rule      ON alert_firing(rule_id, fired_at DESC);
