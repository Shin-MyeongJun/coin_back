SELECT DISTINCT ON (m.exchange_id)
    t.base,
    t.market_code_id,
    t.timestamp,
    t.bid,
    t.ask.
    m.exchange_id,
    m.tradeingPair
    m.base,
    m.quote
FROM market_code m
    JOIN tick t ON t.market_code_id = m.id
WHERE m.base = :base
ORDER BY m.exchange_id, t.timestamp DESC;
