SELECT DISTINCT ON (p.base_exchange_id, p.compare_exchange_id, p.symbol)
    p.id,
    p.symbol,
    p.base_exchange_id,
    p.compare_exchange_id,
    p."timestamp",
    p.bid,
    p.ask
FROM premium p
WHERE p.base_exchange_id = :baseExchangeId
  AND p.compare_exchange_id = :compareExchangeId
ORDER BY p.base_exchange_id,
    p.compare_exchange_id,
    p.symbol,
    p."timestamp" DESC,
    p.id DESC;