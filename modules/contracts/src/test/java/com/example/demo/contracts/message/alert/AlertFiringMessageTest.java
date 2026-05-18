package com.example.demo.contracts.message.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

class AlertFiringMessageTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void roundTripSerialization() throws Exception {
        AlertFiringMessage original = new AlertFiringMessage(
                1L,
                "user-1",
                "BTC premium",
                "PREMIUM BTC >= 3.0",
                new BigDecimal("3.42"),
                1_700_000_000_000L
        );

        String json = mapper.writeValueAsString(original);
        AlertFiringMessage decoded = mapper.readValue(json, AlertFiringMessage.class);

        Assertions.assertEquals(original, decoded);
    }

    @Test
    void jsonFieldNamesAndOrderSnapshot() throws Exception {
        AlertFiringMessage msg = new AlertFiringMessage(
                1L,
                "user-1",
                "BTC premium",
                "PREMIUM BTC >= 3.0",
                new BigDecimal("3.50000000"),
                1700000000000L
        );

        String json = mapper.writeValueAsString(msg);

        Assertions.assertTrue(json.contains("\"ruleId\":1"));
        Assertions.assertTrue(json.contains("\"userId\":\"user-1\""));
        Assertions.assertTrue(json.contains("\"ruleLabel\":\"BTC premium\""));
        Assertions.assertTrue(json.contains("\"conditionText\":\"PREMIUM BTC >= 3.0\""));
        Assertions.assertTrue(json.contains("\"observedValue\":3.50000000"));
        Assertions.assertTrue(json.contains("\"firedAt\":1700000000000"));

        String expected =
                "{\"ruleId\":1,"
                        + "\"userId\":\"user-1\","
                        + "\"ruleLabel\":\"BTC premium\","
                        + "\"conditionText\":\"PREMIUM BTC >= 3.0\","
                        + "\"observedValue\":3.50000000,"
                        + "\"firedAt\":1700000000000}";
        Assertions.assertEquals(expected, json);
    }

    @Test
    void extractKeyUsesUserAndRule() {
        AlertFiringMessage msg = new AlertFiringMessage(
                7L,
                "user-1",
                "BTC premium",
                "PREMIUM BTC >= 3.0",
                new BigDecimal("3.5"),
                1700000000000L
        );

        Assertions.assertEquals("user-1:7", msg.extractKey());
    }
}
