package com.example.demo.benchmarks.json;

import com.dslplatform.json.DslJson;
import com.dslplatform.json.JsonReader;
import com.dslplatform.json.JsonWriter;
import com.example.demo.contracts.message.price_value.TickMessage;
import com.example.demo.infra_shard.json.JsonUtil;
import com.example.demo.infra_shard.json.dsl_json.DslJsonParserManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jsoniter.JsonIterator;
import com.jsoniter.JsonIteratorPool;
import com.jsoniter.output.JsonStream;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * TickMessage 전용 벤치마크. 같은 record 를 두 변형으로 측정해서 "현재 운영" 과
 * "가능 최선" 의 갭을 정량화한다.
 *
 * <h3>AsIs 변형 (현재 운영 경로)</h3>
 * <ul>
 *     <li>Jackson: {@code ObjectMapper.readValue/writeValueAsString} — record native 지원</li>
 *     <li>Jsoniter: {@link JsonUtil#fromJson(String, Class)}/{@link JsonUtil#toJson(Object)}
 *         — codec 등록 없이 reflection 경로. Jsoniter 0.9.23 이 record 를 처리하지 못하면
 *         setup 단계에서 감지하고 {@link IllegalStateException} 으로 fail-fast.</li>
 *     <li>DSL-JSON: {@link DslTickMessage} 어댑터 왕복 — 변환 비용 포함.</li>
 * </ul>
 *
 * <h3>Optimized 변형 (최적 가능 경로)</h3>
 * <ul>
 *     <li>Jackson: 동일 (개선 여지 없음).</li>
 *     <li>Jsoniter: hand-written codec 을 글로벌 SPI 등록 없이 직접 호출. 즉, AsIs 경로와
 *         서로 영향을 주지 않도록 {@link JsoniterSpi.registerTypeDecoder} 를 쓰지 않는다.</li>
 *     <li>DSL-JSON: {@link DslJson#tryFindReader}/{@link DslJson#tryFindWriter} 로 컴파일된
 *         descriptor 를 캐시 후 직접 사용. {@code TickMessage} 에 {@code @CompiledJson} 이
 *         아직 적용돼 있지 않으면 setup 에서 감지하고 벤치마크 호출은 fail-fast 한다 —
 *         별도 PR 로 {@code :contracts} 에 annotationProcessor + {@code @CompiledJson} 을
 *         추가하면 활성화된다.</li>
 * </ul>
 *
 * <h3>Batch fixture</h3>
 * 단조 반복은 CPU 캐시 효과로 결과를 부풀린다. 따라서 {@code fixtures/tick-message-batch/}
 * 의 변종 16개 이상을 {@code variants[i % variants.length]} 로 분산해 batch 를 채운다.
 */
@BenchmarkMode({Mode.Throughput, Mode.AverageTime})
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
public class TickMessageBenchmark {

    private static final String TICK_MESSAGE_ADAPTER_TYPE = "tickMessageAdapter";
    private static final String BATCH_FIXTURE_DIR = "fixtures/tick-message-batch/";
    private static final int BATCH_SIZE = 1_000;
    private static final int MAX_VARIANT_SCAN = 1_000;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final DslJsonParserManager dslJsonParserManager = new DslJsonParserManager();
    private final DslJson<Object> dslJsonRaw = new DslJson<>();
    private final DslJson<Object> dslJsonAdapter = new DslJson<>();

    @Param({"JACKSON", "JSONITER", "DSL_JSON"})
    public Parser parser;

    private String tickMessageJson;
    private String[] tickMessageVariants;
    private String[] tickMessageBatch;
    private TickMessage tickMessage;

    private JsonReader.ReadObject<TickMessage> dslTickReader;
    private JsonWriter.WriteObject<TickMessage> dslTickWriter;
    private boolean optimizedDslAvailable;
    private String optimizedDslReason;

    private boolean asIsJsoniterDeserializeAvailable;
    private String asIsJsoniterDeserializeError;
    private boolean asIsJsoniterSerializeAvailable;
    private String asIsJsoniterSerializeError;

    @Setup(Level.Trial)
    public void setup() {
        registerDslJsonAdapter(dslJsonParserManager);

        tickMessageJson = readFixture("fixtures/tick-message.json");
        tickMessageVariants = loadBatchFixtures(BATCH_FIXTURE_DIR);
        if (tickMessageVariants.length == 0) {
            throw new IllegalStateException(
                    "No tick-message batch variants found under " + BATCH_FIXTURE_DIR);
        }
        tickMessageBatch = new String[BATCH_SIZE];
        for (int i = 0; i < BATCH_SIZE; i++) {
            tickMessageBatch[i] = tickMessageVariants[i % tickMessageVariants.length];
        }
        tickMessage = readJackson(tickMessageJson, TickMessage.class);

        // AsIs Jsoniter: codec 미등록 reflection 경로의 실제 가용성을 한 번만 점검.
        try {
            TickMessage probe = JsonUtil.fromJson(tickMessageJson, TickMessage.class);
            asIsJsoniterDeserializeAvailable = probe != null
                    && probe.marketCodeId() != null
                    && probe.bid() != null
                    && probe.ask() != null
                    && probe.timestamp() != null;
        } catch (Throwable t) {
            asIsJsoniterDeserializeAvailable = false;
            asIsJsoniterDeserializeError = t.getClass().getSimpleName() + ": " + t.getMessage();
        }
        try {
            String probe = JsonUtil.toJson(tickMessage);
            asIsJsoniterSerializeAvailable = probe != null
                    && probe.contains("marketCodeId")
                    && probe.contains("bid")
                    && probe.contains("ask")
                    && probe.contains("timestamp");
        } catch (Throwable t) {
            asIsJsoniterSerializeAvailable = false;
            asIsJsoniterSerializeError = t.getClass().getSimpleName() + ": " + t.getMessage();
        }

        // Optimized DSL: TickMessage 에 직접 적용된 @CompiledJson 의 descriptor 캐시.
        dslTickReader = dslJsonRaw.tryFindReader(TickMessage.class);
        dslTickWriter = dslJsonRaw.tryFindWriter(TickMessage.class);
        optimizedDslAvailable = (dslTickReader != null && dslTickWriter != null);
        if (!optimizedDslAvailable) {
            optimizedDslReason = "TickMessage @CompiledJson not registered — "
                    + "apply annotationProcessor on :contracts (별도 PR)";
        }

        logRuntimePathDiagnostics();
        validateParserParity();
    }

    public static void registerDslJsonAdapter(DslJsonParserManager manager) {
        manager.register(TICK_MESSAGE_ADAPTER_TYPE, DslTickMessage.class);
    }

    // ---------- AsIs 변형 ----------

    @Benchmark
    public TickMessage tickMessageDeserializeAsIs() {
        return readAsIs(tickMessageJson);
    }

    @Benchmark
    public String tickMessageSerializeAsIs() {
        return switch (parser) {
            case JACKSON -> writeJackson(tickMessage);
            case JSONITER -> {
                if (!asIsJsoniterSerializeAvailable) {
                    throw new IllegalStateException(
                            "Jsoniter 운영 경로(JsonUtil.toJson) 가 TickMessage record 를 처리하지 못함 — "
                                    + asIsJsoniterSerializeError);
                }
                yield JsonUtil.toJson(tickMessage);
            }
            case DSL_JSON -> writeDslAdapter(tickMessage);
        };
    }

    @Benchmark
    public long tickMessageBatch1000DeserializeAsIs() {
        long checksum = 0L;
        for (String json : tickMessageBatch) {
            TickMessage message = readAsIs(json);
            checksum += message.marketCodeId();
            checksum += message.timestamp();
        }
        return checksum;
    }

    private TickMessage readAsIs(String json) {
        return switch (parser) {
            case JACKSON -> readJackson(json, TickMessage.class);
            case JSONITER -> {
                if (!asIsJsoniterDeserializeAvailable) {
                    throw new IllegalStateException(
                            "Jsoniter 운영 경로(JsonUtil.fromJson) 가 TickMessage record 를 처리하지 못함 — "
                                    + asIsJsoniterDeserializeError);
                }
                yield JsonUtil.fromJson(json, TickMessage.class);
            }
            case DSL_JSON -> readDslAdapter(json);
        };
    }

    // ---------- Optimized 변형 ----------

    @Benchmark
    public TickMessage tickMessageDeserializeOptimized() {
        return switch (parser) {
            case JACKSON -> readJackson(tickMessageJson, TickMessage.class);
            case JSONITER -> readJsoniterDirect(tickMessageJson);
            case DSL_JSON -> readDslDirect(tickMessageJson);
        };
    }

    @Benchmark
    public String tickMessageSerializeOptimized() {
        return switch (parser) {
            case JACKSON -> writeJackson(tickMessage);
            case JSONITER -> writeJsoniterDirect(tickMessage);
            case DSL_JSON -> writeDslDirect(tickMessage);
        };
    }

    // ---------- 파서별 헬퍼 ----------

    private <T> T readJackson(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String writeJackson(TickMessage message) {
        try {
            return objectMapper.writeValueAsString(message);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TickMessage readDslAdapter(String json) {
        DslTickMessage adapter = dslJsonParserManager.parse(TICK_MESSAGE_ADAPTER_TYPE, json);
        return adapter.toTickMessage();
    }

    private String writeDslAdapter(TickMessage message) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream(128);
            dslJsonAdapter.serialize(DslTickMessage.from(message), out);
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private TickMessage readJsoniterDirect(String json) {
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        JsonIterator iter = JsonIteratorPool.borrowJsonIterator();
        try {
            iter.reset(bytes, 0, bytes.length);
            return decodeTickMessage(iter);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } finally {
            JsonIteratorPool.returnJsonIterator(iter);
        }
    }

    private String writeJsoniterDirect(TickMessage message) {
        ByteArrayOutputStream out = new ByteArrayOutputStream(128);
        JsonStream stream = new JsonStream(out, 128);
        try {
            encodeTickMessage(message, stream);
            stream.close();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return out.toString(StandardCharsets.UTF_8);
    }

    private TickMessage readDslDirect(String json) {
        if (!optimizedDslAvailable) {
            throw new IllegalStateException(optimizedDslReason);
        }
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        try {
            JsonReader<Object> reader = dslJsonRaw.newReader(bytes);
            reader.getNextToken();
            return dslTickReader.read(reader);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private String writeDslDirect(TickMessage message) {
        if (!optimizedDslAvailable) {
            throw new IllegalStateException(optimizedDslReason);
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(128);
        JsonWriter writer = dslJsonRaw.newWriter(128);
        try {
            dslTickWriter.write(writer, message);
            writer.toStream(out);
            return out.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    // ---------- DIAG / parity ----------

    private void logRuntimePathDiagnostics() {
        String dslPath;
        if (dslTickWriter == null) {
            dslPath = "REFLECTION-FALLBACK or MISSING (variant2 will fail)";
        } else if (dslTickWriter.getClass().getName().startsWith("com.dslplatform.json.runtime")) {
            dslPath = "REFLECTION";
        } else {
            dslPath = "COMPILED";
        }
        System.out.println("[DIAG] DSL-JSON TickMessage path: " + dslPath);

        if (asIsJsoniterDeserializeAvailable) {
            System.out.println("[DIAG] Jsoniter TickMessage reflection deserialize: WORKS");
        } else {
            System.out.println("[DIAG] Jsoniter TickMessage reflection deserialize: FAILS — "
                    + asIsJsoniterDeserializeError);
        }
        if (asIsJsoniterSerializeAvailable) {
            System.out.println("[DIAG] Jsoniter TickMessage reflection serialize: WORKS");
        } else {
            System.out.println("[DIAG] Jsoniter TickMessage reflection serialize: FAILS — "
                    + asIsJsoniterSerializeError);
        }
    }

    private void validateParserParity() {
        TickMessage truth = readJackson(tickMessageJson, TickMessage.class);

        // AsIs paths
        requireSameTick(truth, readDslAdapter(tickMessageJson));
        if (asIsJsoniterDeserializeAvailable) {
            requireSameTick(truth, JsonUtil.fromJson(tickMessageJson, TickMessage.class));
        }

        // Optimized paths
        requireSameTick(truth, readJsoniterDirect(tickMessageJson));
        if (optimizedDslAvailable) {
            requireSameTick(truth, readDslDirect(tickMessageJson));
        }
    }

    private static void requireSameTick(TickMessage expected, TickMessage actual) {
        if (!Objects.equals(expected.marketCodeId(), actual.marketCodeId())
                || expected.bid().compareTo(actual.bid()) != 0
                || expected.ask().compareTo(actual.ask()) != 0
                || !Objects.equals(expected.timestamp(), actual.timestamp())) {
            throw new IllegalStateException("TickMessage parser parity failed: expected="
                    + expected + ", actual=" + actual);
        }
    }

    // ---------- fixture 로딩 ----------

    private static String readFixture(String name) {
        try (InputStream inputStream = TickMessageBenchmark.class.getClassLoader().getResourceAsStream(name)) {
            InputStream fixture = Objects.requireNonNull(inputStream, "Fixture not found: " + name);
            return new String(fixture.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String[] loadBatchFixtures(String dir) {
        List<String> variants = new ArrayList<>();
        for (int i = 1; i <= MAX_VARIANT_SCAN; i++) {
            String name = String.format("%stick-%03d.json", dir, i);
            try (InputStream inputStream = TickMessageBenchmark.class.getClassLoader().getResourceAsStream(name)) {
                if (inputStream == null) {
                    break;
                }
                variants.add(new String(inputStream.readAllBytes(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return variants.toArray(new String[0]);
    }

    // ---------- Jsoniter hand-written codec (Optimized 변형 전용) ----------

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

    private static void encodeTickMessage(TickMessage message, JsonStream stream) throws IOException {
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
