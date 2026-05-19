-- :alert WatchlistItemEntity mapping (Bundle A)
-- Renumbered from V3 to V12 to stay contiguous with V10/V11 in this module.
CREATE TABLE IF NOT EXISTS watchlist_item (
    id                   BIGSERIAL    PRIMARY KEY,
    user_id              VARCHAR(64)  NOT NULL,
    market_code_id       BIGINT       NOT NULL,
    symbol               VARCHAR(32),
    domestic_exchange_id BIGINT,
    offshore_exchange_id BIGINT,
    display_order        INTEGER      NOT NULL,
    memo                 VARCHAR(255),
    created_at           BIGINT       NOT NULL,
    updated_at           BIGINT       NOT NULL,
    CONSTRAINT uk_watchlist_item_user_market UNIQUE (user_id, market_code_id)
);

CREATE INDEX IF NOT EXISTS idx_watchlist_item_user ON watchlist_item(user_id, display_order, id);
