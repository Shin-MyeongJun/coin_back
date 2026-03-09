WITH pairs AS (
    SELECT t.type, i.interval
    FROM unnest(CAST(:types AS text[])) WITH ORDINALITY AS t(type, ord)
             JOIN unnest(CAST(:intervals AS text[])) WITH ORDINALITY AS i(interval, ord)
                  ON t.ord = i.ord
),
     filtered AS (
         SELECT
             pi.id,
             pi.market_code_id
             pi."type",
             pi.interval,
             pi.period,
             pi.value,
             pi.bucket_open_ts,
             pi.bucket_close_ts
         FROM premium_inidcator pi
                  JOIN pairs p
                       ON pi."type" = p.type
                           AND pi.interval = p.interval
         WHERE pi.market_code_id = :marketCodeId
           AND pi.base_exchange_id = :baseExchangeId
           AND pi.compare_exchange_id = :compareExchangeId
     ),
     ranked AS (
         SELECT f.*,
                row_number() OVER (
           PARTITION BY f."type", f.interval
           ORDER BY f.bucket_close_ts DESC, f.id DESC
         ) AS rn
         FROM filtered f
     )
SELECT
    id,
    symbol,
    "type",
    interval,
    period,
    value,
    bucket_open_ts,
    bucket_close_ts
FROM ranked
WHERE rn = 1