SELECT DISTINCT ON (market_code_id)
    market_code_id,
    timestamp,
    bid,
    ask
FROM tick
WHERE market_code_id = ANY(:marketCodeIds)
ORDER BY market_code_id, timestamp DESC, id DESC
