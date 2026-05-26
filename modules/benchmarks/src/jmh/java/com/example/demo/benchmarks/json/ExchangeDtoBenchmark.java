package com.example.demo.benchmarks.json;

import com.example.demo.infra_shard.json.JsonUtil;
import com.example.demo.infra_shard.json.dsl_json.DslJsonParserManager;
import com.example.demo.infre_exchange.dto.stream.BinanceBookTickerDto;
import com.example.demo.infre_exchange.dto.stream.BinanceStreamFormat;
import com.example.demo.infre_exchange.upbit.dto.UpbitOrderbookDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 운영 hot path(거래소 WebSocket raw → DTO) 의 실측 대표값을 만드는 벤치마크.
 *
 * <p>의도적으로 세 파서 모두 운영 코드와 동일한 호출만 사용한다.
 * <ul>
 *     <li>Jackson: {@code ObjectMapper#readValue} (Binance 는 wrapper 라서 {@link TypeReference})</li>
 *     <li>Jsoniter: {@link JsonUtil#fromJson(String, Class)} — hand-written codec 등록 금지</li>
 *     <li>DSL-JSON: {@link DslJsonParserManager#parse(String, String)} — 운영 DTO 는 이미
 *         {@code @CompiledJson} 으로 컴파일된 descriptor 가 ServiceLoader 로 잡힌다.</li>
 * </ul>
 *
 * <p>운영에서 거래소 DTO 를 다시 직렬화하는 경로는 없으므로 serialize 측정은 두지 않는다.
 * Jsoniter 0.9.23 이 record 를 reflective 하게 처리하지 못해 setup 단계에서 실패하면
 * {@link #upbitJsoniterAvailable}/{@link #binanceJsoniterAvailable} 로 표시되고, 해당 파서의
 * 벤치마크 호출은 {@link IllegalStateException} 으로 fail-fast 하여 JMH 결과에 명시된다.
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class ExchangeDtoBenchmark {

    private static final String UPBIT_ORDERBOOK_TYPE = "upbitOrderbook";
    private static final String BINANCE_TICK_TYPE = "binanceTick";
    private static final TypeReference<BinanceStreamFormat<BinanceBookTickerDto>> BINANCE_BOOK_TICKER_REF =
            new TypeReference<>() {
            };

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DslJsonParserManager dslJsonParserManager = new DslJsonParserManager();

    @Param({"JACKSON", "JSONITER", "DSL_JSON"})
    public Parser parser;

    private String upbitOrderbookJson;
    private String binanceBookTickerJson;

    private boolean upbitJsoniterAvailable;
    private String upbitJsoniterError;
    private boolean binanceJsoniterAvailable;
    private String binanceJsoniterError;

    @Setup(Level.Trial)
    public void setup() {
        registerDslJsonTypes(dslJsonParserManager);

        upbitOrderbookJson = readFixture("fixtures/upbit-orderbook.json");
        binanceBookTickerJson = readFixture("fixtures/binance-book-ticker.json");

        // 운영 경로(JsonUtil.fromJson) 가 record 를 reflective 하게 처리할 수 있는지 setup 에서 한 번만 점검.
        try {
            UpbitOrderbookDto probe = JsonUtil.fromJson(upbitOrderbookJson, UpbitOrderbookDto.class);
            upbitJsoniterAvailable = probe != null && probe.cd() != null;
        } catch (Throwable t) {
            upbitJsoniterAvailable = false;
            upbitJsoniterError = t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        try {
            @SuppressWarnings("unchecked")
            BinanceStreamFormat<BinanceBookTickerDto> probe =
                    (BinanceStreamFormat<BinanceBookTickerDto>) JsonUtil.fromJson(binanceBookTickerJson, BinanceStreamFormat.class);
            binanceJsoniterAvailable = probe != null && probe.data() != null && probe.data().s() != null;
        } catch (Throwable t) {
            binanceJsoniterAvailable = false;
            binanceJsoniterError = t.getClass().getSimpleName() + ": " + t.getMessage();
        }

        logRuntimePathDiagnostics();
        validateParserParity();
    }

    public static void registerDslJsonTypes(DslJsonParserManager manager) {
        manager.register(UPBIT_ORDERBOOK_TYPE, UpbitOrderbookDto.class);
        manager.registerGeneric(BINANCE_TICK_TYPE, BinanceStreamFormat.class, BinanceBookTickerDto.class);
    }

    @Benchmark
    public UpbitOrderbookDto upbitOrderbookDeserialize() {
        return switch (parser) {
            case JACKSON -> readJackson(upbitOrderbookJson, UpbitOrderbookDto.class);
            case JSONITER -> {
                if (!upbitJsoniterAvailable) {
                    throw new IllegalStateException(
                            "Jsoniter 운영 경로가 UpbitOrderbookDto 를 처리하지 못함 — " + upbitJsoniterError);
                }
                yield JsonUtil.fromJson(upbitOrderbookJson, UpbitOrderbookDto.class);
            }
            case DSL_JSON -> dslJsonParserManager.parse(UPBIT_ORDERBOOK_TYPE, upbitOrderbookJson);
        };
    }

    @Benchmark
    public BinanceBookTickerDto binanceBookTickerDeserialize() {
        return switch (parser) {
            case JACKSON -> readJacksonBinanceBookTicker(binanceBookTickerJson).data();
            case JSONITER -> {
                if (!binanceJsoniterAvailable) {
                    throw new IllegalStateException(
                            "Jsoniter 운영 경로가 BinanceStreamFormat 을 처리하지 못함 — " + binanceJsoniterError);
                }
                yield readJsoniterBinanceBookTicker(binanceBookTickerJson).data();
            }
            case DSL_JSON -> readDslBinanceBookTicker(binanceBookTickerJson).data();
        };
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

    private void logRuntimePathDiagnostics() {
        System.out.println("[DIAG] ExchangeDtoBenchmark UpbitOrderbookDto Jsoniter reflection path: "
                + (upbitJsoniterAvailable ? "WORKS" : "FAILS — " + upbitJsoniterError));
        System.out.println("[DIAG] ExchangeDtoBenchmark BinanceStreamFormat Jsoniter reflection path: "
                + (binanceJsoniterAvailable ? "WORKS" : "FAILS — " + binanceJsoniterError));
    }

    private void validateParserParity() {
        UpbitOrderbookDto jacksonOrderbook = readJackson(upbitOrderbookJson, UpbitOrderbookDto.class);
        UpbitOrderbookDto dslOrderbook = dslJsonParserManager.parse(UPBIT_ORDERBOOK_TYPE, upbitOrderbookJson);
        requireSameOrderbook(jacksonOrderbook, dslOrderbook);
        if (upbitJsoniterAvailable) {
            UpbitOrderbookDto jsoniterOrderbook = JsonUtil.fromJson(upbitOrderbookJson, UpbitOrderbookDto.class);
            requireSameOrderbook(jacksonOrderbook, jsoniterOrderbook);
        }

        BinanceStreamFormat<BinanceBookTickerDto> jacksonBookTicker = readJacksonBinanceBookTicker(binanceBookTickerJson);
        BinanceStreamFormat<BinanceBookTickerDto> dslBookTicker = readDslBinanceBookTicker(binanceBookTickerJson);
        requireSameBookTicker(jacksonBookTicker, dslBookTicker);
        if (binanceJsoniterAvailable) {
            BinanceStreamFormat<BinanceBookTickerDto> jsoniterBookTicker = readJsoniterBinanceBookTicker(binanceBookTickerJson);
            requireSameBookTicker(jacksonBookTicker, jsoniterBookTicker);
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
        try (InputStream inputStream = ExchangeDtoBenchmark.class.getClassLoader().getResourceAsStream(name)) {
            InputStream fixture = Objects.requireNonNull(inputStream, "Fixture not found: " + name);
            return new String(fixture.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public enum Parser {
        JACKSON,
        JSONITER,
        DSL_JSON
    }
}
