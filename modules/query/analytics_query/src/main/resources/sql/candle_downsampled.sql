SELECT
    NULL                                                                    AS id,
    :marketCodeId                                                           AS market_code_id,
    NULL                                                                    AS symbol,
    NULL                                                                    AS base_exchange_id,
    NULL                                                                    AS compare_exchange_id,
    :interval                                                               AS interval,
    first(open, bucket_open_ts)                                             AS open,
    max(high)                                                               AS high,
    min(low)                                                                AS low,
    last(close, bucket_close_ts)                                            AS close,
    extract(epoch FROM time_bucket(
        make_interval(secs => :targetBucketSeconds),
        to_timestamp(bucket_open_ts / 1000.0)
    ))::bigint * 1000                                                       AS bucket_open_ts,
    extract(epoch FROM time_bucket(
        make_interval(secs => :targetBucketSeconds),
        to_timestamp(bucket_open_ts / 1000.0)
    ) + make_interval(secs => :targetBucketSeconds))::bigint * 1000 - 1    AS bucket_close_ts,
    NULL                                                                    AS observe_open_ts,
    NULL                                                                    AS observe_close_ts
FROM tick_candle
WHERE market_code_id = :marketCodeId
  AND interval        = :interval
  AND bucket_open_ts BETWEEN :fromTs AND :toTs
GROUP BY time_bucket(make_interval(secs => :targetBucketSeconds), to_timestamp(bucket_open_ts / 1000.0))
ORDER BY bucket_open_ts
