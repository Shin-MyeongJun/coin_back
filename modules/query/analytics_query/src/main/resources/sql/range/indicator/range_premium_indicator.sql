WITH pairs AS (
    SELECT t.type, i.interval
    FROM unnest(CAST(:types AS text[])) WITH ORDINALITY AS t(type, ord)
             JOIN unnest(CAST(:intervals AS text[])) WITH ORDINALITY AS i(interval, ord)
                  ON t.ord = i.ord
)
SELECT pi.id,
       pi.symbol,
       pi.base_exchange_id,
       pi.compare_exchange_id,
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
WHERE pi.symbol = :symbol
  AND pi.base_exchange_id = :baseExchangeId
  AND pi.compare_exchange_id = :compareExchangeId;
AND pi.bucket_open_ts BETWEEN :fromTs AND :toTs