WITH pairs AS (
    SELECT t.type, i.interval
    FROM unnest(CAST(:types AS text[])) WITH ORDINALITY AS t(type, ord)
             JOIN unnest(CAST(:intervals AS text[])) WITH ORDINALITY AS i(interval, ord)
                  ON t.ord = i.ord
)
SELECT ti.id,
       ti.market_code_id
       ti."type",
       ti.interval,
       ti.period,
       ti.value,
       ti.bucket_open_ts,
       ti.bucket_close_ts
FROM tick_inidcator ti
         JOIN pairs p
              ON ti."type" = p.type
                  AND ti.interval = p.interval
WHERE ti.market_code_id = :marketCodeId
  AND ti.bucket_open_ts BETWEEN :fromTs AND :toTs