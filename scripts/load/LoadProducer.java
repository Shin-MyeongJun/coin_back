import com.example.demo.contracts.message.raw.TickRawMessage;
import java.time.Duration;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

public final class LoadProducer {

    private static final int DEFAULT_TARGET_TPS = 15_000;
    private static final int DEFAULT_DURATION_SEC = 120;
    private static final int DEFAULT_MARKET_COUNT = 1_500;
    private static final String DEFAULT_BOOTSTRAP_SERVERS = "localhost:9092";
    private static final String DEFAULT_TOPIC = "ingestion-exchange.tick-raw";
    private static final long NANOS_PER_SECOND = Duration.ofSeconds(1).toNanos();

    private LoadProducer() {
    }

    public static void main(String[] args) {
        Options options = Options.parse(args);
        MarketTemplate[] markets = MarketTemplate.create(options.marketCount());
        long expectedMessages = (long) options.targetTps() * options.durationSec();
        LatencyRecorder latencyRecorder = new LatencyRecorder(expectedMessages + options.targetTps());
        AtomicLong acked = new AtomicLong();
        AtomicLong errors = new AtomicLong();
        AtomicReference<String> firstError = new AtomicReference<>();

        Properties properties = producerProperties(options);
        long sent = 0L;
        long startedAt = System.nanoTime();
        long loadEndAt = startedAt + Duration.ofSeconds(options.durationSec()).toNanos();
        long nextReportAt = startedAt + NANOS_PER_SECOND;
        long lastReportAt = startedAt;
        long lastSent = 0L;
        long lastAcked = 0L;

        System.err.printf(
                Locale.ROOT,
                "starting targetTps=%d durationSec=%d bootstrapServers=%s topic=%s marketCount=%d%n",
                options.targetTps(),
                options.durationSec(),
                options.bootstrapServers(),
                options.topic(),
                options.marketCount()
        );

        TokenBucket limiter = new TokenBucket(options.targetTps());
        try (Producer<String, String> producer = new KafkaProducer<>(properties)) {
            while (System.nanoTime() < loadEndAt) {
                limiter.acquire();
                long sequence = sent;
                TickRawMessage message = nextMessage(markets, sequence);
                String value = toJson(message);
                ProducerRecord<String, String> record = new ProducerRecord<>(options.topic(), message.base(), value);
                long sendStartedAt = System.nanoTime();

                try {
                    producer.send(record, (metadata, exception) -> {
                        latencyRecorder.record(System.nanoTime() - sendStartedAt);
                        if (exception == null) {
                            acked.incrementAndGet();
                        }
                        else {
                            errors.incrementAndGet();
                            firstError.compareAndSet(null, exception.getClass().getName() + ": " + exception.getMessage());
                        }
                    });
                    sent++;
                }
                catch (RuntimeException exception) {
                    errors.incrementAndGet();
                    firstError.compareAndSet(null, exception.getClass().getName() + ": " + exception.getMessage());
                }

                long now = System.nanoTime();
                if (now >= nextReportAt) {
                    long currentAcked = acked.get();
                    double elapsedSec = (now - lastReportAt) / 1_000_000_000.0;
                    double sentTps = (sent - lastSent) / elapsedSec;
                    double ackTps = (currentAcked - lastAcked) / elapsedSec;
                    System.err.printf(
                            Locale.ROOT,
                            "elapsed=%ds sent=%d acked=%d sendTps=%.0f ackTps=%.0f errors=%d%n",
                            (now - startedAt) / NANOS_PER_SECOND,
                            sent,
                            currentAcked,
                            sentTps,
                            ackTps,
                            errors.get()
                    );
                    lastReportAt = now;
                    lastSent = sent;
                    lastAcked = currentAcked;
                    while (nextReportAt <= now) {
                        nextReportAt += NANOS_PER_SECOND;
                    }
                }
            }

            producer.flush();
        }

        long completedAt = System.nanoTime();
        long totalAcked = acked.get();
        long totalErrors = errors.get();
        double sendWindowSec = options.durationSec();
        double totalElapsedSec = (completedAt - startedAt) / 1_000_000_000.0;
        double averageSendTps = sent / sendWindowSec;
        double averageAckTps = totalAcked / totalElapsedSec;
        double p95LatencyMs = latencyRecorder.p95Millis();

        System.err.printf(
                Locale.ROOT,
                "completed sent=%d acked=%d errors=%d averageSendTps=%.2f averageAckTps=%.2f p95ProduceLatencyMs=%.3f elapsedSec=%.3f%n",
                sent,
                totalAcked,
                totalErrors,
                averageSendTps,
                averageAckTps,
                p95LatencyMs,
                totalElapsedSec
        );
        if (firstError.get() != null) {
            System.err.printf(Locale.ROOT, "firstError=%s%n", firstError.get());
        }

        System.out.println(summaryJson(options, sent, totalAcked, totalErrors, averageSendTps, averageAckTps, p95LatencyMs, totalElapsedSec));
        if (totalErrors > 0) {
            System.exit(2);
        }
    }

    private static Properties producerProperties(Options options) {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, options.bootstrapServers());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "coindata-load-producer-" + ProcessHandle.current().pid());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.ACKS_CONFIG, "1");
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, Integer.toString(64 * 1024));
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, Long.toString(128L * 1024L * 1024L));
        properties.put(ProducerConfig.LINGER_MS_CONFIG, "20");
        properties.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "none");
        properties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, "10000");
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, "120000");
        properties.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, "30000");
        return properties;
    }

    private static TickRawMessage nextMessage(MarketTemplate[] markets, long sequence) {
        MarketTemplate market = markets[(int) (sequence % markets.length)];
        long drift = sequence % 10_000L;
        long bidCents = market.baseBidCents() + drift;
        long askCents = bidCents + 5L;
        return new TickRawMessage(
                market.tradingPair(),
                market.exchange(),
                market.exchangeType(),
                market.quote(),
                market.base(),
                decimal(bidCents),
                decimal(askCents),
                System.currentTimeMillis()
        );
    }

    private static String decimal(long cents) {
        long whole = cents / 100L;
        long fraction = Math.abs(cents % 100L);
        return whole + "." + (fraction < 10L ? "0" : "") + fraction;
    }

    private static String toJson(TickRawMessage message) {
        return "{"
                + "\"tradingPair\":\"" + escape(message.tradingPair()) + "\","
                + "\"exchange\":\"" + escape(message.exchange()) + "\","
                + "\"exchangeType\":\"" + escape(message.exchangeType()) + "\","
                + "\"quote\":\"" + escape(message.quote()) + "\","
                + "\"base\":\"" + escape(message.base()) + "\","
                + "\"bid\":\"" + escape(message.bid()) + "\","
                + "\"ask\":\"" + escape(message.ask()) + "\","
                + "\"timestamp\":" + message.timestamp()
                + "}";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder escaped = new StringBuilder(value.length() + 8);
        for (int index = 0; index < value.length(); index++) {
            char current = value.charAt(index);
            switch (current) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (current < 0x20) {
                        escaped.append(String.format(Locale.ROOT, "\\u%04x", (int) current));
                    }
                    else {
                        escaped.append(current);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private static String summaryJson(
            Options options,
            long sent,
            long acked,
            long errors,
            double averageSendTps,
            double averageAckTps,
            double p95LatencyMs,
            double elapsedSec
    ) {
        return String.format(
                Locale.ROOT,
                "{\"targetTps\":%d,\"durationSec\":%d,\"bootstrapServers\":\"%s\",\"topic\":\"%s\",\"marketCount\":%d,"
                        + "\"sent\":%d,\"acked\":%d,\"errors\":%d,\"averageSendTps\":%.2f,\"averageAckTps\":%.2f,"
                        + "\"p95ProduceLatencyMs\":%.3f,\"elapsedSec\":%.3f}",
                options.targetTps(),
                options.durationSec(),
                escape(options.bootstrapServers()),
                escape(options.topic()),
                options.marketCount(),
                sent,
                acked,
                errors,
                averageSendTps,
                averageAckTps,
                p95LatencyMs,
                elapsedSec
        );
    }

    private record Options(int targetTps, int durationSec, String bootstrapServers, String topic, int marketCount) {

        private static Options parse(String[] args) {
            int targetTps = DEFAULT_TARGET_TPS;
            int durationSec = DEFAULT_DURATION_SEC;
            String bootstrapServers = DEFAULT_BOOTSTRAP_SERVERS;
            String topic = DEFAULT_TOPIC;
            int marketCount = DEFAULT_MARKET_COUNT;

            for (int index = 0; index < args.length; index++) {
                String arg = args[index];
                switch (arg) {
                    case "--target-tps" -> targetTps = positiveInt(arg, valueAt(args, ++index, arg));
                    case "--duration-sec" -> durationSec = positiveInt(arg, valueAt(args, ++index, arg));
                    case "--bootstrap-servers" -> bootstrapServers = valueAt(args, ++index, arg);
                    case "--topic" -> topic = valueAt(args, ++index, arg);
                    case "--market-count" -> marketCount = positiveInt(arg, valueAt(args, ++index, arg));
                    case "--help", "-h" -> {
                        printUsage();
                        System.exit(0);
                    }
                    default -> {
                        System.err.println("unknown argument: " + arg);
                        printUsage();
                        System.exit(1);
                    }
                }
            }

            return new Options(targetTps, durationSec, bootstrapServers, topic, marketCount);
        }

        private static String valueAt(String[] args, int index, String option) {
            if (index >= args.length) {
                System.err.println("missing value for " + option);
                printUsage();
                System.exit(1);
            }
            return args[index];
        }

        private static int positiveInt(String option, String value) {
            try {
                int parsed = Integer.parseInt(value);
                if (parsed <= 0) {
                    throw new NumberFormatException("must be positive");
                }
                return parsed;
            }
            catch (NumberFormatException exception) {
                System.err.printf(Locale.ROOT, "invalid %s value: %s%n", option, value);
                printUsage();
                System.exit(1);
                return -1;
            }
        }

        private static void printUsage() {
            System.err.println("Usage: java LoadProducer [--target-tps 15000] [--duration-sec 120] [--bootstrap-servers localhost:9092] [--topic ingestion-exchange.tick-raw] [--market-count 1500]");
        }
    }

    private record MarketTemplate(String tradingPair, String exchange, String exchangeType, String quote, String base, long baseBidCents) {

        private static MarketTemplate[] create(int count) {
            MarketTemplate[] markets = new MarketTemplate[count];
            for (int index = 0; index < count; index++) {
                String base = "LOAD" + leftPad(index + 1, 4);
                String quote = (index % 2 == 0) ? "USDT" : "KRW";
                String exchange = (index % 2 == 0) ? "BINANCE" : "UPBIT";
                long baseBidCents = 10_000_00L + (index * 37L);
                markets[index] = new MarketTemplate(base + quote, exchange, "SPOT", quote, base, baseBidCents);
            }
            return markets;
        }

        private static String leftPad(int value, int width) {
            String text = Integer.toString(value);
            if (text.length() >= width) {
                return text;
            }
            return "0".repeat(width - text.length()) + text;
        }
    }

    private static final class TokenBucket {

        private final double capacity;
        private final double tokensPerNano;
        private double tokens;
        private long lastRefillNanos;

        private TokenBucket(int permitsPerSecond) {
            this.capacity = Math.max(1.0, permitsPerSecond / 10.0);
            this.tokens = capacity;
            this.tokensPerNano = permitsPerSecond / 1_000_000_000.0;
            this.lastRefillNanos = System.nanoTime();
        }

        private void acquire() {
            while (true) {
                long now = System.nanoTime();
                refill(now);
                if (tokens >= 1.0) {
                    tokens -= 1.0;
                    return;
                }

                long waitNanos = (long) Math.ceil((1.0 - tokens) / tokensPerNano);
                LockSupport.parkNanos(Math.min(waitNanos, 1_000_000L));
            }
        }

        private void refill(long now) {
            long elapsed = now - lastRefillNanos;
            if (elapsed <= 0L) {
                return;
            }

            tokens = Math.min(capacity, tokens + (elapsed * tokensPerNano));
            lastRefillNanos = now;
        }
    }

    private static final class LatencyRecorder {

        private static final int MAX_SAMPLES = 10_000_000;
        private final long[] samples;
        private final AtomicLong nextIndex = new AtomicLong();

        private LatencyRecorder(long expectedMessages) {
            int sampleCount = (int) Math.min(Math.max(1L, expectedMessages), MAX_SAMPLES);
            this.samples = new long[sampleCount];
        }

        private void record(long nanos) {
            long index = nextIndex.getAndIncrement();
            if (index < samples.length) {
                samples[(int) index] = nanos;
            }
        }

        private double p95Millis() {
            int used = (int) Math.min(nextIndex.get(), samples.length);
            if (used == 0) {
                return 0.0;
            }

            long[] copy = Arrays.copyOf(samples, used);
            Arrays.sort(copy);
            int index = Math.max(0, (int) Math.ceil(used * 0.95) - 1);
            return copy[index] / 1_000_000.0;
        }
    }
}
