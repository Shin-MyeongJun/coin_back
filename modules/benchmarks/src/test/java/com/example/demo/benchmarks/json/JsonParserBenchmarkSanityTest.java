package com.example.demo.benchmarks.json;

import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.infra_shard.json.JsonUtil;
import com.example.demo.infra_shard.json.dsl_json.DslJsonParserManager;
import com.example.demo.infre_exchange.dto.stream.BinanceBookTickerDto;
import com.example.demo.infre_exchange.dto.stream.BinanceStreamFormat;
import com.example.demo.infre_exchange.upbit.dto.UpbitOrderbookDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.spi.JsoniterSpi;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonParserBenchmarkSanityTest {

    private static final String TICK_MESSAGE_TYPE = "tickMessage";
    private static final String UPBIT_ORDERBOOK_TYPE = "upbitOrderbook";
    private static final String BINANCE_TICK_TYPE = "binanceTick";
    private static final TypeReference<BinanceStreamFormat<BinanceBookTickerDto>> BINANCE_BOOK_TICKER_REF =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void allParsersDeserializeFixturesToEquivalentObjects() throws IOException {
        DslJsonParserManager manager = new DslJsonParserManager();
        registerJsoniterCodecs();
        manager.register(TICK_MESSAGE_TYPE, TestDslTickMessage.class);
        manager.register(UPBIT_ORDERBOOK_TYPE, UpbitOrderbookDto.class);
        manager.registerGeneric(BINANCE_TICK_TYPE, BinanceStreamFormat.class, BinanceBookTickerDto.class);

        String tickJson = readFixture("tick-message.json");
        TickMessage jacksonTick = objectMapper.readValue(tickJson, TickMessage.class);
        TickMessage jsoniterTick = JsonUtil.fromJson(tickJson, TickMessage.class);
        TestDslTickMessage dslJsonTick = manager.parse(TICK_MESSAGE_TYPE, tickJson);

        assertEquals(jacksonTick.marketCodeId(), jsoniterTick.marketCodeId());
        assertEquals(0, jacksonTick.bid().compareTo(jsoniterTick.bid()));
        assertEquals(0, jacksonTick.ask().compareTo(jsoniterTick.ask()));
        assertEquals(jacksonTick.timestamp(), jsoniterTick.timestamp());
        assertEquals(jacksonTick.marketCodeId(), dslJsonTick.marketCodeId());
        assertEquals(0, jacksonTick.bid().compareTo(dslJsonTick.bid()));
        assertEquals(0, jacksonTick.ask().compareTo(dslJsonTick.ask()));
        assertEquals(jacksonTick.timestamp(), dslJsonTick.timestamp());

        String orderbookJson = readFixture("upbit-orderbook.json");
        UpbitOrderbookDto jacksonOrderbook = objectMapper.readValue(orderbookJson, UpbitOrderbookDto.class);
        UpbitOrderbookDto jsoniterOrderbook = JsonUtil.fromJson(orderbookJson, UpbitOrderbookDto.class);
        UpbitOrderbookDto dslOrderbook = manager.parse(UPBIT_ORDERBOOK_TYPE, orderbookJson);

        assertOrderbook(jacksonOrderbook, jsoniterOrderbook);
        assertOrderbook(jacksonOrderbook, dslOrderbook);

        String binanceJson = readFixture("binance-book-ticker.json");
        BinanceStreamFormat<BinanceBookTickerDto> jacksonBookTicker = objectMapper.readValue(binanceJson, BINANCE_BOOK_TICKER_REF);
        BinanceStreamFormat<BinanceBookTickerDto> jsoniterBookTicker = readJsoniterBinanceBookTicker(binanceJson);
        BinanceStreamFormat<BinanceBookTickerDto> dslBookTicker = manager.parse(BINANCE_TICK_TYPE, binanceJson);

        assertBookTicker(jacksonBookTicker, jsoniterBookTicker);
        assertBookTicker(jacksonBookTicker, dslBookTicker);
    }

    private static void assertOrderbook(UpbitOrderbookDto expected, UpbitOrderbookDto actual) {
        assertEquals(expected.ty(), actual.ty());
        assertEquals(expected.cd(), actual.cd());
        assertEquals(expected.tms(), actual.tms());
        assertEquals(0, expected.tas().compareTo(actual.tas()));
        assertEquals(0, expected.tbs().compareTo(actual.tbs()));
        assertEquals(expected.obu().size(), actual.obu().size());
        assertEquals(0, expected.obu().get(0).ap().compareTo(actual.obu().get(0).ap()));
        assertEquals(0, expected.obu().get(0).bp().compareTo(actual.obu().get(0).bp()));
        assertEquals(0, expected.obu().get(0).as().compareTo(actual.obu().get(0).as()));
        assertEquals(0, expected.obu().get(0).bs().compareTo(actual.obu().get(0).bs()));
        assertEquals(expected.lv(), actual.lv());
    }

    private static void assertBookTicker(
            BinanceStreamFormat<BinanceBookTickerDto> expected,
            BinanceStreamFormat<BinanceBookTickerDto> actual
    ) {
        BinanceBookTickerDto expectedData = expected.data();
        BinanceBookTickerDto actualData = actual.data();
        assertEquals(expected.stream(), actual.stream());
        assertEquals(expectedData.e(), actualData.e());
        assertEquals(expectedData.u(), actualData.u());
        assertEquals(expectedData.E(), actualData.E());
        assertEquals(expectedData.T(), actualData.T());
        assertEquals(expectedData.s(), actualData.s());
        assertEquals(expectedData.b(), actualData.b());
        assertEquals(expectedData.B(), actualData.B());
        assertEquals(expectedData.a(), actualData.a());
        assertEquals(expectedData.A(), actualData.A());
    }

    private static void registerJsoniterCodecs() {
        JsoniterSpi.registerTypeDecoder(TickMessage.class, JsonParserBenchmarkSanityTest::decodeTickMessage);
        JsoniterSpi.registerTypeDecoder(UpbitOrderbookDto.class, JsonParserBenchmarkSanityTest::decodeUpbitOrderbook);
        JsoniterSpi.registerTypeDecoder(UpbitOrderbookDto.OrderbookUnit.class, JsonParserBenchmarkSanityTest::decodeOrderbookUnit);
        JsoniterSpi.registerTypeDecoder(BinanceStreamFormat.class, JsonParserBenchmarkSanityTest::decodeBinanceStreamFormat);
        JsoniterSpi.registerTypeDecoder(BinanceBookTickerDto.class, JsonParserBenchmarkSanityTest::decodeBinanceBookTicker);
    }

    private static TickMessage decodeTickMessage(JsonIterator iter) throws IOException {
        Long marketCodeId = null;
        BigDecimal bid = null;
        BigDecimal ask = null;
        Long timestamp = null;

        for (String field = iter.readObject(); field != null; field = iter.readObject()) {
            switch (field) {
                case "marketCodeId" -> marketCodeId = iter.readLong();
                case "bid" -> bid = iter.readBigDecimal();
                case "ask" -> ask = iter.readBigDecimal();
                case "timestamp" -> timestamp = iter.readLong();
                default -> iter.skip();
            }
        }
        return new TickMessage(marketCodeId, bid, ask, timestamp);
    }

    private static UpbitOrderbookDto decodeUpbitOrderbook(JsonIterator iter) throws IOException {
        String ty = null;
        String cd = null;
        long tms = 0L;
        BigDecimal tas = null;
        BigDecimal tbs = null;
        List<UpbitOrderbookDto.OrderbookUnit> obu = List.of();
        double lv = 0D;

        for (String field = iter.readObject(); field != null; field = iter.readObject()) {
            switch (field) {
                case "ty" -> ty = iter.readString();
                case "cd" -> cd = iter.readString();
                case "tms" -> tms = iter.readLong();
                case "tas" -> tas = iter.readBigDecimal();
                case "tbs" -> tbs = iter.readBigDecimal();
                case "obu" -> obu = decodeOrderbookUnits(iter);
                case "lv" -> lv = iter.readDouble();
                default -> iter.skip();
            }
        }
        return new UpbitOrderbookDto(ty, cd, tms, tas, tbs, obu, lv);
    }

    private static List<UpbitOrderbookDto.OrderbookUnit> decodeOrderbookUnits(JsonIterator iter) throws IOException {
        List<UpbitOrderbookDto.OrderbookUnit> units = new ArrayList<>();
        while (iter.readArray()) {
            units.add(decodeOrderbookUnit(iter));
        }
        return units;
    }

    private static UpbitOrderbookDto.OrderbookUnit decodeOrderbookUnit(JsonIterator iter) throws IOException {
        BigDecimal ap = null;
        BigDecimal bp = null;
        BigDecimal as = null;
        BigDecimal bs = null;

        for (String field = iter.readObject(); field != null; field = iter.readObject()) {
            switch (field) {
                case "ap" -> ap = iter.readBigDecimal();
                case "bp" -> bp = iter.readBigDecimal();
                case "as" -> as = iter.readBigDecimal();
                case "bs" -> bs = iter.readBigDecimal();
                default -> iter.skip();
            }
        }
        return new UpbitOrderbookDto.OrderbookUnit(ap, bp, as, bs);
    }

    private static BinanceStreamFormat<BinanceBookTickerDto> decodeBinanceStreamFormat(JsonIterator iter) throws IOException {
        String stream = null;
        BinanceBookTickerDto data = null;

        for (String field = iter.readObject(); field != null; field = iter.readObject()) {
            switch (field) {
                case "stream" -> stream = iter.readString();
                case "data" -> data = decodeBinanceBookTicker(iter);
                default -> iter.skip();
            }
        }
        return new BinanceStreamFormat<>(stream, data);
    }

    private static BinanceBookTickerDto decodeBinanceBookTicker(JsonIterator iter) throws IOException {
        String e = null;
        long u = 0L;
        long eventTime = 0L;
        long transactionTime = 0L;
        String s = null;
        String b = null;
        String bidQty = null;
        String a = null;
        String askQty = null;

        for (String field = iter.readObject(); field != null; field = iter.readObject()) {
            switch (field) {
                case "e" -> e = iter.readString();
                case "u" -> u = iter.readLong();
                case "E" -> eventTime = iter.readLong();
                case "T" -> transactionTime = iter.readLong();
                case "s" -> s = iter.readString();
                case "b" -> b = iter.readString();
                case "B" -> bidQty = iter.readString();
                case "a" -> a = iter.readString();
                case "A" -> askQty = iter.readString();
                default -> iter.skip();
            }
        }
        return new BinanceBookTickerDto(e, u, eventTime, transactionTime, s, b, bidQty, a, askQty);
    }

    @SuppressWarnings("unchecked")
    private static BinanceStreamFormat<BinanceBookTickerDto> readJsoniterBinanceBookTicker(String json) {
        return (BinanceStreamFormat<BinanceBookTickerDto>) JsonUtil.fromJson(json, BinanceStreamFormat.class);
    }

    private static String readFixture(String fixtureName) throws IOException {
        Path path = Path.of("src", "jmh", "resources", "fixtures", fixtureName);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
