SELECT DISTINCT ON (t.market_code_id)
    t.id,
    t.market_code_id,
    t.timestamp,
    t.bid,
    t.ask,
FROM tick t
ORDER BY t.market_code_id, t.timestamp DESC;