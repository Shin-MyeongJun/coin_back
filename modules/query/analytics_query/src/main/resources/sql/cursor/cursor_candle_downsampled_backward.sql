SELECT *
FROM (
    SELECT
        :marketCodeId                                                           AS market_code_id,
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
    GROUP BY time_bucket(make_interval(secs => :targetBucketSeconds), to_timestamp(bucket_open_ts / 1000.0))
) sub
WHERE (:cursor IS NULL OR sub.bucket_open_ts <= :cursor)
ORDER BY sub.bucket_open_ts DESC
LIMIT :limit
