package com.example.demo.benchmarks.json;

import com.dslplatform.json.DslJson;
import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.infra_shard.json.JsonUtil;
import com.example.demo.infra_shard.json.dsl_json.DslJsonParserManager;
import com.example.demo.infre_exchange.dto.stream.BinanceBookTickerDto;
import com.example.demo.infre_exchange.dto.stream.BinanceStreamFormat;
import com.example.demo.infre_exchange.upbit.dto.UpbitOrderbookDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.JsoniterSpi;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class JsonParserBenchmark {

    private static final String TICK_MESSAGE_TYPE = "tickMessage";
    private static final String UPBIT_ORDERBOOK_TYPE = "upbitOrderbook";
    private static final String BINANCE_TICK_TYPE = "binanceTick";
    private static final int BATCH_SIZE = 1_000;
    private static final AtomicBoolean JSONITER_CODECS_REGISTERED = new AtomicBoolean(false);
    private static final TypeReference<BinanceStreamFormat<BinanceBookTickerDto>> BINANCE_BOOK_TICKER_REF =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DslJsonParserManager dslJsonParserManager = new DslJsonParserManager();
    private final DslJson<Object> dslJson = new DslJson<>();

    @Param({"JACKSON", "JSONITER", "DSL_JSON"})
    public Parser parser;

    private String tickMessageJson;
    private String upbitOrderbookJson;
    private String binanceBookTickerJson;
    private String[] tickMessageBatch;
    private TickMessage tickMessage;

    @Setup(Level.Trial)
    public void setup() {
        registerJsoniterCodecs();
        registerDslJsonTypes(dslJsonParserManager);

        tickMessageJson = readFixture("fixtures/tick-message.json");
        upbitOrderbookJson = readFixture("fixtures/upbit-orderbook.json");
        binanceBookTickerJson = readFixture("fixtures/binance-book-ticker.json");
        tickMessageBatch = new String[BATCH_SIZE];
        Arrays.fill(tickMessageBatch, tickMessageJson);
        tickMessage = readJackson(tickMessageJson, TickMessage.class);

        validateParserParity();
    }

    public static void registerJsoniterCodecs() {
        if (!JSONITER_CODECS_REGISTERED.compareAndSet(false, true)) {
            return;
        }
        // Jsoniter 0.9.23 does not instantiate Java records reflectively.
        JsoniterSpi.registerTypeDecoder(TickMessage.class, JsonParserBenchmark::decodeTickMessage);
        JsoniterSpi.registerTypeDecoder(UpbitOrderbookDto.class, JsonParserBenchmark::decodeUpbitOrderbook);
        JsoniterSpi.registerTypeDecoder(UpbitOrderbookDto.OrderbookUnit.class, JsonParserBenchmark::decodeOrderbookUnit);
        JsoniterSpi.registerTypeDecoder(BinanceStreamFormat.class, JsonParserBenchmark::decodeBinanceStreamFormat);
        JsoniterSpi.registerTypeDecoder(BinanceBookTickerDto.class, JsonParserBenchmark::decodeBinanceBookTicker);
        JsoniterSpi.registerTypeEncoder(TickMessage.class, JsonParserBenchmark::encodeTickMessage);
    }

    public static void registerDslJsonTypes(DslJsonParserManager manager) {
        manager.register(TICK_MESSAGE_TYPE, DslTickMessage.class);
        manager.register(UPBIT_ORDERBOOK_TYPE, UpbitOrderbookDto.class);
        manager.registerGeneric(BINANCE_TICK_TYPE, BinanceStreamFormat.class, BinanceBookTickerDto.class);
    }

    @Benchmark
    public TickMessage tickMessageDeserialize() {
        return readTickMessage(tickMessageJson);
    }

    @Benchmark
    public String tickMessageSerialize() {
        return switch (parser) {
            case JACKSON -> writeJackson(tickMessage);
            case JSONITER -> JsonUtil.toJson(tickMessage);
            case DSL_JSON -> writeDslJson(tickMessage);
        };
    }

    @Benchmark
    public long tickMessageBatch1000Deserialize() {
        long checksum = 0L;
        for (String json : tickMessageBatch) {
            TickMessage message = readTickMessage(json);
            checksum += message.marketCodeId();
            checksum += message.timestamp();
        }
        return checksum;
    }

    @Benchmark
    public UpbitOrderbookDto upbitOrderbookDeserialize() {
        return switch (parser) {
            case JACKSON -> readJackson(upbitOrderbookJson, UpbitOrderbookDto.class);
            case JSONITER -> JsonUtil.fromJson(upbitOrderbookJson, UpbitOrderbookDto.class);
            case DSL_JSON -> dslJsonParserManager.parse(UPBIT_ORDERBOOK_TYPE, upbitOrderbookJson);
        };
    }

    @Benchmark
    public BinanceBookTickerDto binanceBookTickerDeserialize() {
        return switch (parser) {
            case JACKSON -> readJacksonBinanceBookTicker(binanceBookTickerJson).data();
            case JSONITER -> readJsoniterBinanceBookTicker(binanceBookTickerJson).data();
            case DSL_JSON -> readDslBinanceBookTicker(binanceBookTickerJson).data();
        };
    }

    private TickMessage readTickMessage(String json) {
        return switch (parser) {
            case JACKSON -> readJackson(json, TickMessage.class);
            case JSONITER -> JsonUtil.fromJson(json, TickMessage.class);
            case DSL_JSON -> readDslTickMessage(json);
        };
    }

    private TickMessage readDslTickMessage(String json) {
        DslTickMessage message = dslJsonParserManager.parse(TICK_MESSAGE_TYPE, json);
        return message.toTickMessage();
    }

    private <MESSAGE> MESSAGE readJackson(String json, Class<MESSAGE> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private BinanceStreamFormat<BinanceBookTickerDto> readJacksonBinanceBookTicker(String json) {
        try {
            return objectMapper.readValue(json, BINANCE_BOOK_TICKER_REF);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private BinanceStreamFormat<BinanceBookTickerDto> readJsoniterBinanceBookTicker(String json) {
        return (BinanceStreamFormat<BinanceBookTickerDto>) JsonUtil.fromJson(json, BinanceStreamFormat.class);
    }

    private BinanceStreamFormat<BinanceBookTickerDto> readDslBinanceBookTicker(String json) {
        return dslJsonParserManager.parse(BINANCE_TICK_TYPE, json);
    }

    private String writeJackson(TickMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String writeDslJson(TickMessage message) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream(128);
            dslJson.serialize(DslTickMessage.from(message), outputStream);
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private void validateParserParity() {
        TickMessage jacksonTick = readJackson(tickMessageJson, TickMessage.class);
        TickMessage jsoniterTick = JsonUtil.fromJson(tickMessageJson, TickMessage.class);
        TickMessage dslJsonTick = readDslTickMessage(tickMessageJson);
        requireSameTick(jacksonTick, jsoniterTick);
        requireSameTick(jacksonTick, dslJsonTick);

        UpbitOrderbookDto jacksonOrderbook = readJackson(upbitOrderbookJson, UpbitOrderbookDto.class);
        UpbitOrderbookDto jsoniterOrderbook = JsonUtil.fromJson(upbitOrderbookJson, UpbitOrderbookDto.class);
        UpbitOrderbookDto dslOrderbook = dslJsonParserManager.parse(UPBIT_ORDERBOOK_TYPE, upbitOrderbookJson);
        requireSameOrderbook(jacksonOrderbook, jsoniterOrderbook);
        requireSameOrderbook(jacksonOrderbook, dslOrderbook);

        BinanceStreamFormat<BinanceBookTickerDto> jacksonBookTicker = readJacksonBinanceBookTicker(binanceBookTickerJson);
        BinanceStreamFormat<BinanceBookTickerDto> jsoniterBookTicker = readJsoniterBinanceBookTicker(binanceBookTickerJson);
        BinanceStreamFormat<BinanceBookTickerDto> dslBookTicker = readDslBinanceBookTicker(binanceBookTickerJson);
        requireSameBookTicker(jacksonBookTicker, jsoniterBookTicker);
        requireSameBookTicker(jacksonBookTicker, dslBookTicker);
    }

    private static void requireSameTick(TickMessage expected, TickMessage actual) {
        if (!Objects.equals(expected.marketCodeId(), actual.marketCodeId())
                || expected.bid().compareTo(actual.bid()) != 0
                || expected.ask().compareTo(actual.ask()) != 0
                || !Objects.equals(expected.timestamp(), actual.timestamp())) {
            throw new IllegalStateException("TickMessage parser parity failed");
        }
    }

    private static void requireSameOrderbook(UpbitOrderbookDto expected, UpbitOrderbookDto actual) {
        if (!Objects.equals(expected.ty(), actual.ty())
                || !Objects.equals(expected.cd(), actual.cd())
                || expected.tms() != actual.tms()
                || expected.tas().compareTo(actual.tas()) != 0
                || expected.tbs().compareTo(actual.tbs()) != 0
                || Double.compare(expected.lv(), actual.lv()) != 0
                || expected.obu().size() != actual.obu().size()
                || expected.obu().get(0).ap().compareTo(actual.obu().get(0).ap()) != 0
                || expected.obu().get(0).bp().compareTo(actual.obu().get(0).bp()) != 0
                || expected.obu().get(0).as().compareTo(actual.obu().get(0).as()) != 0
                || expected.obu().get(0).bs().compareTo(actual.obu().get(0).bs()) != 0) {
            throw new IllegalStateException("UpbitOrderbookDto parser parity failed");
        }
    }

    private static void requireSameBookTicker(
            BinanceStreamFormat<BinanceBookTickerDto> expected,
            BinanceStreamFormat<BinanceBookTickerDto> actual
    ) {
        BinanceBookTickerDto expectedData = expected.data();
        BinanceBookTickerDto actualData = actual.data();
        if (!Objects.equals(expected.stream(), actual.stream())
                || !Objects.equals(expectedData.e(), actualData.e())
                || expectedData.u() != actualData.u()
                || expectedData.E() != actualData.E()
                || expectedData.T() != actualData.T()
                || !Objects.equals(expectedData.s(), actualData.s())
                || !Objects.equals(expectedData.b(), actualData.b())
                || !Objects.equals(expectedData.B(), actualData.B())
                || !Objects.equals(expectedData.a(), actualData.a())
                || !Objects.equals(expectedData.A(), actualData.A())) {
            throw new IllegalStateException("BinanceBookTickerDto parser parity failed");
        }
    }

    private static String readFixture(String name) {
        try (InputStream inputStream = JsonParserBenchmark.class.getClassLoader().getResourceAsStream(name)) {
            InputStream fixture = Objects.requireNonNull(inputStream, "Fixture not found: " + name);
            return new String(fixture.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
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

    private static void encodeTickMessage(Object object, JsonStream stream) throws IOException {
        TickMessage message = (TickMessage) object;
        stream.writeObjectStart();
        stream.writeObjectField("marketCodeId");
        stream.writeVal(message.marketCodeId());
        stream.writeMore();
        stream.writeObjectField("bid");
        stream.writeRaw(message.bid().toPlainString());
        stream.writeMore();
        stream.writeObjectField("ask");
        stream.writeRaw(message.ask().toPlainString());
        stream.writeMore();
        stream.writeObjectField("timestamp");
        stream.writeVal(message.timestamp());
        stream.writeObjectEnd();
    }

    public enum Parser {
        JACKSON,
        JSONITER,
        DSL_JSON
    }
}
