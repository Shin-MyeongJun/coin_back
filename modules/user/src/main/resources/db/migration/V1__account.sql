-- account table for :user module
CREATE TABLE IF NOT EXISTS account (
    id              UUID         PRIMARY KEY,
    email           VARCHAR(320) NOT NULL,
    password_hash   VARCHAR(100) NOT NULL,
    tier            VARCHAR(16)  NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL,
    updated_at      TIMESTAMPTZ  NOT NULL,
    CONSTRAINT uk_account_email UNIQUE (email)
);
