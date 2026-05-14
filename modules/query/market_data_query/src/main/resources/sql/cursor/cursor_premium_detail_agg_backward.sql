SELECT *
FROM (
    SELECT
        symbol,
        base_exchange_id,
        compare_exchange_id,
        extract(epoch FROM time_bucket(
            make_interval(secs => :bucketSeconds),
            to_timestamp(timestamp / 1000.0)
        ))::bigint * 1000                                                       AS bucket_ts,
        avg(compare_bid / NULLIF(base_bid, 0) - 1)                              AS bid,
        avg(compare_ask / NULLIF(base_ask, 0) - 1)                              AS ask
    FROM premium_detail
    WHERE base_exchange_id    = :baseExchangeId
      AND compare_exchange_id = :compareExchangeId
      AND symbol              = :symbol
    GROUP BY symbol, base_exchange_id, compare_exchange_id,
             time_bucket(make_interval(secs => :bucketSeconds), to_timestamp(timestamp / 1000.0))
) sub
WHERE (:cursor IS NULL OR sub.bucket_ts <= :cursor)
ORDER BY sub.bucket_ts DESC
LIMIT :limit
