package com.example.demo.contracts.message.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AlertFiringMessageTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void roundTripSerialization() throws Exception {
        AlertFiringMessage original = AlertFiringMessage.of(
                "rule-1",
                "acc-1",
                "PREMIUM",
                "Upbit-Binance BTC premium crossed 3%",
                3.42,
                3.0,
                "CROSS_UP",
                1_700_000_000_000L
        );

        String json = mapper.writeValueAsString(original);
        AlertFiringMessage decoded = mapper.readValue(json, AlertFiringMessage.class);

        Assertions.assertEquals(original, decoded);
    }

    @Test
    void jsonFieldNamesAndOrderSnapshot() throws Exception {
        AlertFiringMessage msg = AlertFiringMessage.of(
                "rule-1",
                "acc-1",
                "PREMIUM",
                "summary-text",
                3.5,
                3.0,
                "ABOVE",
                1700000000000L
        );

        String json = mapper.writeValueAsString(msg);

        // 필드 이름 회귀 방지
        Assertions.assertTrue(json.contains("\"ruleId\":\"rule-1\""));
        Assertions.assertTrue(json.contains("\"accountId\":\"acc-1\""));
        Assertions.assertTrue(json.contains("\"ruleType\":\"PREMIUM\""));
        Assertions.assertTrue(json.contains("\"summary\":\"summary-text\""));
        Assertions.assertTrue(json.contains("\"observedValue\":3.5"));
        Assertions.assertTrue(json.contains("\"thresholdValue\":3.0"));
        Assertions.assertTrue(json.contains("\"direction\":\"ABOVE\""));
        Assertions.assertTrue(json.contains("\"firedAt\":1700000000000"));

        // 필드 순서 회귀 방지 (record 선언 순서를 Jackson이 그대로 따른다)
        String expected =
                "{\"ruleId\":\"rule-1\","
                        + "\"accountId\":\"acc-1\","
                        + "\"ruleType\":\"PREMIUM\","
                        + "\"summary\":\"summary-text\","
                        + "\"observedValue\":3.5,"
                        + "\"thresholdValue\":3.0,"
                        + "\"direction\":\"ABOVE\","
                        + "\"firedAt\":1700000000000}";
        Assertions.assertEquals(expected, json);
    }

    @Test
    void staticFactoryRejectsNullStrings() {
        Assertions.assertThrows(NullPointerException.class, () -> AlertFiringMessage.of(
                null, "acc", "PREMIUM", "s", 1, 1, "ABOVE", 0L));
        Assertions.assertThrows(NullPointerException.class, () -> AlertFiringMessage.of(
                "r", null, "PREMIUM", "s", 1, 1, "ABOVE", 0L));
        Assertions.assertThrows(NullPointerException.class, () -> AlertFiringMessage.of(
                "r", "acc", null, "s", 1, 1, "ABOVE", 0L));
        Assertions.assertThrows(NullPointerException.class, () -> AlertFiringMessage.of(
                "r", "acc", "PREMIUM", null, 1, 1, "ABOVE", 0L));
        Assertions.assertThrows(NullPointerException.class, () -> AlertFiringMessage.of(
                "r", "acc", "PREMIUM", "s", 1, 1, null, 0L));
    }
}
