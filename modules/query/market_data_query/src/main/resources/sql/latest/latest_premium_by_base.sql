SELECT DISTINCT ON (base_exchange_id, compare_exchange_id)
    symbol,
    base_exchange_id,
    compare_exchange_id,
    timestamp,
    bid,
    ask
FROM premium
WHERE symbol LIKE :base || '%'
ORDER BY base_exchange_id, compare_exchange_id, timestamp DESC, id DESC
