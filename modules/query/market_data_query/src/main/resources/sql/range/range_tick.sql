SELECT  t.id,
        t.market_code_id,
        t.timestamp,
        t.bid,
        t.ask,
FROM    tick t
WHERE   t.market_code_id =:target  t.timestamp BETWEEN :fromTs AND :toTs
ORDER BY t.timestamp DESC;