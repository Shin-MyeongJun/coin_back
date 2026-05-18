CREATE TABLE alert_rule (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(64) NOT NULL,
  label VARCHAR(50) NOT NULL,
  target_type VARCHAR(16) NOT NULL,
  asset_symbol VARCHAR(16) NOT NULL,
  operator VARCHAR(4) NOT NULL,
  threshold NUMERIC(20, 8) NOT NULL,
  cooldown_sec INT NOT NULL,
  channels VARCHAR(64) NOT NULL,
  active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at BIGINT NOT NULL,
  updated_at BIGINT NOT NULL
);
CREATE INDEX idx_alert_rule_user ON alert_rule(user_id, active);

CREATE TABLE alert_firing (
  id BIGSERIAL PRIMARY KEY,
  rule_id BIGINT NOT NULL,
  user_id VARCHAR(64) NOT NULL,
  rule_label VARCHAR(50) NOT NULL,
  condition_text VARCHAR(64) NOT NULL,
  observed_value NUMERIC(20, 8) NOT NULL,
  fired_at BIGINT NOT NULL
);
CREATE INDEX idx_alert_firing_user_time ON alert_firing(user_id, fired_at DESC);
CREATE INDEX idx_alert_firing_rule ON alert_firing(rule_id, fired_at DESC);
