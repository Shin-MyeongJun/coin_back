SELECT
    e.id             AS exchange_id,
    e.name           AS exchange_name,
    NULL             AS market_code_id,
    NULL             AS trading_pair,
    'EXCHANGE_WITHOUT_MARKET' AS issue_type
FROM exchange e
WHERE NOT EXISTS (
    SELECT 1 FROM market_code mc WHERE mc.exchange_id = e.id
)

UNION ALL

SELECT
    NULL             AS exchange_id,
    NULL             AS exchange_name,
    mc.id            AS market_code_id,
    mc.trading_pair  AS trading_pair,
    'ORPHAN_MARKET_CODE' AS issue_type
FROM market_code mc
WHERE NOT EXISTS (
    SELECT 1 FROM exchange e WHERE e.id = mc.exchange_id
)
