package com.example.demo.ingestion.fx.infrastructure.mapper;

import com.example.demo.contracts.message.fx.FxMessage;
import com.example.demo.infra_shard.messaging.mapper.RawToMessage;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;

@Component
public class NaverRawFxMapper implements RawToMessage<String,FxMessage>  {
    private final String pair_format ="{%s}_{%s}";

    @Override
    public FxMessage toMessage( String s, Map<String, String> args)  {

        String base = args.get("base").toUpperCase();
        String compare =args.get("compare").toUpperCase();
        String fxPair = pair_format.formatted(base,compare);
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var node = mapper.readTree(s);
            String valueStr = node.get("country").get(1).get("value").asText().replace(",", "");
            BigDecimal val = new BigDecimal(valueStr);

            return new FxMessage(
                    fxPair,
                    base,
                    compare,
                    val,
                    System.currentTimeMillis()

            );
        }catch (Exception e){
            return null;
        }
    }

}
