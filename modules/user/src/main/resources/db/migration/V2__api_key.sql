-- api_key table for :user module (Step 3)
CREATE TABLE IF NOT EXISTS api_key (
    id            UUID         PRIMARY KEY,
    account_id    UUID         NOT NULL,
    label         VARCHAR(100) NOT NULL,
    prefix        VARCHAR(8)   NOT NULL,
    hash          VARCHAR(100) NOT NULL,
    scopes        TEXT[]       NOT NULL DEFAULT '{}',
    ip_allowlist  TEXT[]       NOT NULL DEFAULT '{}',
    policy_rpm    INTEGER      NOT NULL,
    policy_rpd    INTEGER      NOT NULL,
    policy_sse    INTEGER      NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,
    revoked_at    TIMESTAMPTZ,
    last_used_at  TIMESTAMPTZ,
    CONSTRAINT uk_api_key_prefix    UNIQUE (prefix),
    CONSTRAINT fk_api_key_account   FOREIGN KEY (account_id) REFERENCES account(id)
);

CREATE INDEX IF NOT EXISTS ix_api_key_account_id ON api_key(account_id);
